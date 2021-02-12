(ns hub.sketchbook.logical-clocks.vector-clock
  "Actual implementation of a basic vector clock."
  (:refer-clojure :exclude [merge]))

;; TODO: prune old entries as the structure grows

(defn now []
  (.getTime (java.util.Date.)))

(defn entry
  ([]
   (entry 1 (now)))
  ([counter timestamp]
   {:counter   counter
    :timestamp timestamp}))

(defn clock [& kv] (apply hash-map kv))

(defn merge [& clocks]
  (letfn [(max-counter [& args]
            (apply max-key :counter args))]
    (apply merge-with max-counter clocks)))

(defn increment [clock node-id]
  (if-let [node (get clock node-id)]
    (assoc clock node-id (entry (inc (:counter node)) (now)))
    (assoc clock node-id (entry))))

(defn descendant? [ancestor-clock descendant-clock]
  (reduce (fn [prev [node-id par-node]]
            (or prev
                (if-let [desc-node (get descendant-clock node-id)]
                  (<= (:counter desc-node) (:counter par-node)))))
          true ancestor-clock))
