(ns hub.500lines.archaeology-db.core
  (:require [hub.500lines.archaeology-db.types
             :refer [entity-at attr-at value-of-at index-at]]))

(defn evolution-of
  "Read into the past of the database.

  Returns a sequence of pairs, each consisting of the timestamp and value of an
  attribute’s update"
  [db entity-id attr-name]
  ;; O(n) as long as prev-ts doesn't cause weird loops
  (loop [res [] ts (:curr-time db)]
    (if (= -1 ts)
      (reverse res)
      (let [attr (attr-at db entity-id attr-name ts)]
        (recur (conj res {(:ts attr) (:value attr)}) (:prev-ts attr))))))
