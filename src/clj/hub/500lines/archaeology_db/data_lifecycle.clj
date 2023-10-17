(ns hub.500lines.archaeology-db.data-lifecycle
  (:require [hub.500lines.archaeology-db.foundation :as db]
            [clojure.set :as set]))

;;;; Adding an Entity

(defn- next-ts [db] (inc (:curr-time db)))

(defn- update-creation-ts [entity ts-val]
  (reduce (fn [acc attr] (assoc-in acc [:attrs attr :ts] ts-val))
          entity
          (keys (:attrs entity))))

(defn- next-id
  "Returns a 2-tuple of entity-id and the db's top-id.
  New entities will increment the DB's top-id and that becomes the entity-id."
  [db entity]
  (let [top-id       (:top-id db)
        entity-id    (:id entity)
        increased-id (inc top-id)]
    (if (= entity-id :db/no-id-yet)
      [(keyword (str increased-id)) increased-id]
      [entity-id top-id])))

(defn- fix-new-entity
  "Returns a 2-tuple of an updated entity and updated db top-id.
  entity-id and top-id either stay the same (for existing entity) or increase
  for new entity. i.e. function is idempotent."
  [db entity]
  (let [[entity-id next-top-id] (next-id db entity)
        new-ts                  (next-ts db)
        new-entity              (update-creation-ts (assoc entity :id entity-id)
                                                    new-ts)]
    [new-entity next-top-id]))

(defn- update-entry-in-index [index path operation]
  (let [update-path       (butlast path)
        update-value      (last path)
        to-be-updated-set (get-in index update-path #{})]
    (assoc-in index update-path (conj to-be-updated-set update-value))))

(defn collify [x] (if (coll? x) x [x]))
(defn- update-attr-in-index [index ent-id attr-name target-val operation]
  (let [colled-target-val (collify target-val)
        update-entry-fn (fn [ind vl]
                          (update-entry-in-index
                           ind
                           ((db/from-eav index) ent-id attr-name vl)
                           operation))]
    (reduce update-entry-fn index colled-target-val)))

(defn- add-entity-to-index  [entity layer index-name]
  (let [entity-id       (:id entity)
        index           (index-name layer)
        all-attrs       (vals (:attrs entity))
        relevant-attrs  (filter (db/usage-pred index) all-attrs)
        add-in-index-fn (fn [index attr]
                          (update-attr-in-index index entity-id (:name attr)
                                                (:value attr)
                                                :db/add))]
    (assoc layer index-name (reduce add-in-index-fn index relevant-attrs))))

(defn add-entity
  "Adds an entity to the database.
  1. Fix the entity, meaning give it an id and make that the top id for the database.
  2. Write this to storage, which is kept in a new top layer
  3. Go through each index and update with the new entity"
  [db entity]
  (let [[fixed-entity next-top-id]
        (fix-new-entity db entity)

        layer-with-updated-storage
        (update (last (:layers db)) :storage
                db/write-entity fixed-entity)

        new-layer (reduce (partial add-entity-to-index fixed-entity)
                          layer-with-updated-storage
                          (db/indexes))]
    (assoc db
           :layers (conj (:layers db) new-layer)
           :top-id next-top-id)))

(defn add-entities [db ents-seq] (reduce add-entity db ents-seq))

;;;; Removing an Entity

(defn- reffing-to [entity-id layer]
  (let [vaet (:VAET layer)]
    (for [[attr-name reffing-set] (entity-id vaet)
          reffing reffing-set]
      [reffing attr-name])))

(declare update-entity)
(defn- remove-back-refs [db entity-id layer]
  (let [reffing-datoms (reffing-to entity-id layer)
        clean-db       (reduce (fn remove-entity [db [reffing-entity attr]]
                                 (update-entity db reffing-entity attr entity-id :db/remove))
                               db reffing-datoms)]
    (last (:layers clean-db))))

(defn- remove-entry-from-index [index path]
  (let [path-to-items (butlast path)
        val-to-remove (last path)
        old-entries   (get-in index path-to-items)]
    (cond
      (not (contains? old-entries val-to-remove))
      index

      ;; last val for nest, remove nest.
      ;; e.g. (remove-entry {:e {:a {:entry #{:bar}}}} [:e :a :entry]) => {:e {}}
      (= 1 (count old-entries))
      (update index (first path) dissoc (second path))

      :else
      (update-in index path-to-items disj val-to-remove))))

(defn- remove-entries-from-index [entity-id _operation index attr]
  ;; TODO: when will we have an operation that ISN'T remove?
  (->>  (collify (:value attr))
        (map (partial (db/from-eav index) entity-id (:name attr)))
        (reduce remove-entry-from-index index)))

(defn- remove-entity-from-index [entity layer index-name]
  (let [index                (index-name layer)
        relevant-attrs       (filter #((db/usage-pred index) %) (vals (:attrs entity)))
        remove-from-index-fn (partial remove-entries-from-index (:id entity) :db/remove)]
    (->> relevant-attrs
         (reduce remove-from-index-fn index)
         (assoc layer index-name))))

(defn remove-entity [db entity-id]
  (let [entity          (db/entity-at db entity-id)
        layer           (remove-back-refs db entity-id (last (:layers db)))
        no-ref-layer    (update layer :VAET dissoc entity-id)
        no-entity-layer (assoc no-ref-layer :storage
                               (db/drop-entity (:storage no-ref-layer) entity))
        new-layer       (reduce (partial remove-entity-from-index entity)
                                no-entity-layer (db/indexes))]))


;;;; Updating an Entity

(defn- update-attr-modification-time [attr new-ts]
  (assoc attr :ts new-ts :prev-ts (:ts attr)))

(defn- update-attr-value [attr value operation]
  (cond
    (db/single? attr)
    (assoc attr :value #{value})

    (= :db/reset-to operation)
    (assoc attr :value value)

    (= :db/add operation)
    (assoc attr :value (clojure.set/union (:value attr) value))

    (= :db/remove operation)
    (assoc attr :value (clojure.set/difference (:value attr) value))))

(defn- update-attr [attr new-val new-ts operation]
  {:pre [(if (db/single? attr)
           (contains? #{:db/reset-to :db-remove} operation)
           (contains? #{:db/reset-to :db-remove :db/add} operation))]}
  (-> attr
      (update-attr-modification-time new-ts)
      (update-attr-value new-val operation)))

(defn- update-index [entity-id attr new-val operation layer index-name]
  (if-not ((db/usage-pred (index-name layer)) attr)
    layer
    (let [cleaned-index (remove-entries-from-index entity-id operation (index-name layer) attr)
          updated-index (if (= operation :db/remove)
                          cleaned-index
                          (update-attr-in-index cleaned-index entity-id
                                                (:name attr) new-val operation))]
      (assoc layer index-name updated-index))))

(defn- update-indexes [layer entity-id attr new-val operation]
  (reduce (partial update-index entity-id attr new-val operation)
          layer
          (db/indexes)))

(defn- put-entity [storage entity-id new-attr]
  (assoc-in (db/get-entity storage entity-id)
            [:attrs (:name new-attr)] new-attr))

(defn- write-entity [storage entity-id new-attr]
  (db/write-entity storage (put-entity storage entity-id new-attr)))

(defn- update-layer [layer entity-id old-attr updated-attr new-val operation]
  (-> layer
      (update-indexes entity-id old-attr new-val operation)
      (update :storage write-entity entity-id updated-attr)))

(defn update-entity
  ([db entity-id attr-name new-val]
   (update-entity db entity-id attr-name new-val :db/reset-to))
  ([db entity-id attr-name new-val operation]
   (let [attr                (db/attr-at db entity-id attr-name)
         updated-attr        (update-attr attr new-val (next-ts db) operation)
         fully-updated-layer (update-layer (last (:layers db)) entity-id
                                           attr updated-attr
                                           new-val operation)]
     (update db :layers conj fully-updated-layer))))

;;;; Transactions

(defn transact-on-db [initial-db ops]
  (loop [[op & remaining] ops
         transacted       initial-db]
    (if op
      ;; every op is a fn with first arg db
      (recur remaining (apply (first op) transacted (rest op)))
      (let [initial-layer   (:layers initial-db)
            new-layer (last (:layers transacted))]
        (assoc initial-db
               :layers (conj initial-layer new-layer)
               ;; only step one time
               :curr-time (next-ts initial-db)
               ;; lots of IDs happen though
               :top-id (:top-id transacted))))))

(defmacro _transact [db op & txs]
  (when txs
    (loop [[first-tx# & rest-tx#] txs
           result#                [op db `transact-on-db]
           accum-txs#             []]
      (if first-tx#
        (recur rest-tx# result# (conj accum-txs# (vec first-tx#)))
        (list* (conj result# accum-txs#))))))

(defmacro transact
  [db-conn & txs]
  `(_transact ~db-conn swap! ~@txs))

(defn- _what-if [db f txs]  (f db txs))
(defmacro what-if [db & ops]  `(_transact ~db _what-if  ~@ops))
