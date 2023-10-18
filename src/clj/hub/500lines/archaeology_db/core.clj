(ns hub.500lines.archaeology-db.core
  "https://github.com/aosabook/500lines/blob/master/functionalDB/functionalDB.markdown"
  (:require [hub.500lines.archaeology-db.foundation :as db]))

;;; TODO:
;;; - implement graph traversals
;;; - show working examples
;;; Extension:
;;; - durability
;;; - dig more into Datomic/Datalog

(defn evolution-of
  "Read into the past of the database.

  Returns a sequence of pairs, each consisting of the timestamp and value of an
  attribute’s update"
  [db entity-id attr-name]
  ;; O(n) as long as prev-ts doesn't cause weird loops
  (loop [res [] ts (:curr-time db)]
    (if (= -1 ts)
      (reverse res)
      (let [attr (db/attr-at db entity-id attr-name ts)]
        (recur (conj res {(:ts attr) (:value attr)}) (:prev-ts attr))))))

;;;; Graph Traversal

(defn incoming-refs [db ts entity-id & ref-names]
  (let [vaet         (db/index-at db :VAET ts)
        all-attr-map (vaet entity-id)
        filtered-map (if ref-names
                       (select-keys ref-names all-attr-map)
                       all-attr-map)]
    (reduce into #{} (vals filtered-map))))

(defn outgoing-refs [db ts entity-id & ref-names]
  (let [val-filter-fn (if ref-names
                        #(vals (select-keys ref-names %))
                        vals)]
    (if-not entity-id
      []
      (->> (db/entity-at db ts entity-id)
           :attrs
           val-filter-fn
           (filter db/ref?)
           (mapcat :value)))))

;;; "Once we have the ability to look at our database as a graph, we
;;; can provide various graph traversing and querying APIs. We leave
;;; this as a solved exercise to the reader; one solution can be found
;;; in the chapter's source code (see graph.clj)."

;;; TL;DR implement BFS or DFS
