(ns hub.500lines.archaeology-db.foundation)


(defrecord Entity [id attrs])

(defn make-entity
  ([] (make-entity :db/no-id-yet))
  ([id] (Entity.  id {})))


(defrecord Attr [name value ts prev-ts])

(defn make-attr
  "
  :cardinality - can be either :db/single or :db/multiple. :db/single is the default."
  [name value type
   & {:keys [cardinality]
      :or   {cardinality :db/single}} ]
  {:pre [(contains? #{:db/single :db/multiple} cardinality)]}
  (with-meta (Attr. name value -1 -1)
    {:type type :cardinality cardinality}))

(defn add-attr [^Entity entity ^Attr attr]
  (let [attr-id (keyword (:name attr))]
    (assoc-in entity [:attrs attr-id] attr)))


(defprotocol Storage
  (get-entity [storage e-id])
  (write-entity [storage ^Entity entity])
  (drop-entity [storage ^Entity entity]))

(defrecord InMemory []
  Storage
  (get-entity [storage entity-id]
    (entity-id storage))
  (write-entity [storage entity]
    (assoc storage (:id entity) entity))
  (drop-entity [storage entity]
    (dissoc storage (:id entity))))


;; E = Entity
;; A = Attribute
;; V = Value
;; T = Time
;; index name is determined by access order, e.g. (get-in db [E A V T])
(defn indexes [] [:VAET :AVET :VEAT :EAVT])
(defn make-index
  "Nested maps, each level is one of Entity Attribute Value. Order may vary.
  `from-eav` and `to-eav` are functions to convert the index between orderings.
  `usage-pred` determines which attributes are used in an index"
  [from-eav to-eav usage-pred]
  (with-meta {}
    {:from-eav from-eav :to-eav to-eav :usage-pred usage-pred}))

(defn from-eav [index] (:from-eav (meta index)))
(defn to-eav [index] (:to-eav (meta index)))
(defn usage-pred [index] (:usage-pred (meta index)))


(defrecord Layer [storage VAET AVET VEAT EAVT])

(defn single? [attr]
  (= :db/single (:cardinality (meta attr))))
(defn ref? [attr] (= :db/ref (:type (meta attr))))
(defn always [& more] true)

(defrecord Database [layers top-id curr-time])
(defn make-db []
  (atom
   (Database.
    [(Layer.
      ;; TODO: where is this from? FoundationDB? what import? what deps?
      (InMemory.) ; storage
      ;; TODO: why is `ref?` in a lambda?
      (make-index #(vector %3 %2 %1) #(vector %3 %2 %1) ref?);VAET
      (make-index #(vector %2 %3 %1) #(vector %3 %1 %2) always);AVET
      (make-index #(vector %3 %1 %2) #(vector %2 %3 %1) always);VEAT
      (make-index #(vector %1 %2 %3) #(vector %1 %2 %3) always);EAVT
      )]
    0 0)))

;;;; Basic accessors

(defn entity-at
  ([db entity-id]
   (entity-at db (:curr-time db) entity-id))
  ([db ts entity-id]
   (get-entity (get-in db [:layers ts :storage]) entity-id)))

(defn attr-at
  ([db entity-id attr-name]
   (attr-at db entity-id attr-name (:curr-time db)))
  ([db entity-id attr-name ts]
   (get-in (entity-at db ts entity-id) [:attrs attr-name])))

(defn value-of-at
  ([db entity-id attr-name]
   (:value (attr-at db entity-id attr-name)))
  ([db entity-id attr-name ts]
   (:value (attr-at db entity-id attr-name ts))))

(defn index-at
  ([db kind]
   (index-at db kind (:curr-time db)))
  ([db kind ts]
   (kind ((:layers db) ts))))
