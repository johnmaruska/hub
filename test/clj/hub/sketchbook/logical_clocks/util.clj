(ns hub.sketchbook.logical-clocks.util
  (:require  [clojure.test :as t]))

(defn simulate [{:keys [send bump init-node] :as fns}]
  (-> (reduce init-node {} [:a :b :c])
      (bump :a)
      (send :a :b)
      (bump :c)
      (bump :a)
      (bump :b)
      (send :b :c)))
