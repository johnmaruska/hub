(ns hub.500lines.archaeology-db.query
  (:require [hub.500lines.archaeology-db.foundation :as db]))

;;; transformation

(defmacro symbol-col-to-set [coll]
  (set (map str coll)))


(defn variable?
  ([x] (variable? x true))
  ([x accept_?]
   (or (and accept_? (= x "_")) (= (first x) \?))))

(defmacro clause-term-expr [clause-term]
  (cond
    (variable? (str clause-term))
    #(= % %)

    (not (coll? clause-term))   ; constant
    `#(= % ~clause-term)

    (= 2 (count clause-term))  ; unary
    `#(~(first clause-term) %)

    (variable? (str (second clause-term)))  ; binary, 1st variable
    `#(~(first clause-term) % ~(last clause-term))

    (variable? (str (last clause-term)))  ; binary, 2nd variable
    `#(~(first clause-term) ~(second clause-term) %)))

(defmacro clause-term-meta [clause-term]
  (cond
    (coll? clause-term)
    (first (filter #(variable? % false) (map str clause-term)))

    (variable? (str clause-term) false)
    (str clause-term)

    :no-variable-in-clause nil))

(defmacro pred-clause [clause]
  (loop [[term# & remaining#] clause
         exprs# []
         metas# []]
    (if term#
      (recur remaining#
             (conj exprs# `clause-term-expr ~ term#)
             (conj metas# `(clause-term-meta ~ term#))))))

(defmacro query-clauses-to-pred-clauses [clauses]
  (loop [[clause# & remaining#] clauses
         preds-vecs# []]
    (if-not clause#
      preds-vecs#
      (recur remaining# `(conj ~preds-vecs# (pred-clause ~clause#))))))

;;; Making a plan

(defn index-of-joining-variable [query-clauses]
  (let [metas-seq (map #(:db/variable (meta %)) query-clauses)
        collapsed (reduce (fn [acc v] (map #(when (= %1 %2) %1) acc v)) metas-seq)]
    (first (keep-indexed #(when (variable? %2 false) %1) collapsed))))

(declare single-index-query-plan)
(defn build-query-plan [query]
  (let [term-index   (index-of-joining-variable query)
        index-to-use (case term-index 0 :AVET 1 :VEAT 2 :EAVT)]
    (partial single-index-query-plan query index-to-use)))

;;; Execution of the Plan

(defn filter-index [index predicate-clauses]
  (for [pred-clause predicate-clauses
        :let [[lvl1-pred lvl2-pred lvl3-pred]
              (apply (db/from-eav index) pred-clause)]

        [k1 l2map] index  ; first level KVs
        :when (try (lvl1-pred k1) (catch Exception e false))

        [k2 l3-set] l2map  ; second level KVs
        :when (try (lvl2-pred k2) (catch Exception e false))
        :let [res (set (filter lvl3-pred l3-set))]]
    (with-meta [k1 k2 res] (meta pred-clause))))

(defn items-that-answer-all-conditions [items-seq num-of-conditions]
  (->> items-seq
       (map vec)
       (reduce into [])  ; flat vector
       frequencies  ; how many collections was each item in
       (filter #(<= num-of-conditions (last %)))  ; answered all conditions
       (map first)  ; get the items
       set))

(defn mask-path-leaf-with-items [relevant-items path]
  (update path 2 clojure.set/intersection relevant-items))

(defn query-index [index pred-clauses]
  (let [result-clauses (filter-index index pred-clauses)
        relevant-items (items-that-answer-all-conditions (map last result-clauses)
                                                         (count pred-clauses))
        cleaned-result-clauses (map (partial mask-path-leaf-with-items relevant-items)
                                    result-clauses)]
    (filter #(not-empty (last %)) cleaned-result-clauses)))

(defn combine-path-and-meta [from-eav-fn path]
  (let [expanded-path [(repeat (first path)) (repeat (second path)) (last path)]
        meta-of-path  (apply from-eav-fn (map repeat (:db/variable (meta path))))
        combined-path (interleave meta-of-path expanded-path)]
    (apply (partial map vector) combined-path)))

(defn bind-variables-to-query [query-result index]
  (->> query-result
       (mapcat (partial combine-path-and-meta (db/from-eav index)))
       (map #(->> %1 (partition 2 (apply (db/to-eav index)))))  ; result paths
       (reduce (fn [acc path] (assoc-in acc (butlast path) (last path))) {})))

(defn single-index-query-plan [query index db]
  (let [query-result (query-index (db/index-at db index) query)]
    (bind-variables-to-query query-result (db/index-at db index))))


;;; Unify

(defn resultify-bind-pair [vars-set accum pair]
  (let [[var-name _] pair]
    (if (contains? vars-set var-name)
      (conj accum pair)
      accum)))

(defn resultify-attr-val-pair [vars-set accum-result attr-val-pair]
  (reduce (partial resultify-bind-pair vars-set) accum-result attr-val-pair))

(defn locate-vars-in-query-result [vars-set query-result]
  (let [[entity-pair attr-val-map] query-result
        entity-result (resultify-bind-pair vars-set [] entity-pair)]
    (map (partial resultify-attr-val-pair vars-set entity-result) attr-val-map)))

(defn unify [binded-result-coll needed-vars]
  (map (partial locate-vars-in-query-result needed-vars) binded-result-coll))

;;; Run

(defmacro q
  [db query]
  `(let [pred-clauses#          (query-clauses-to-pred-clauses ~(:where query))
         needed-vars#           (symbol-col-to-set ~(:find query))
         query-plan#            (build-query-plan pred-clauses#)
         query-internal-result# (query-plan# ~db)]
     (unify query-internal-result# needed-vars#)))
