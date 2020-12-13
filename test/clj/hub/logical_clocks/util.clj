(ns hub.logical-clocks.util
  (:require  [clojure.test :as t]))

(def example
  {:node
   {:a {:nid :a :version 3}
    :b {:nid :b :version 3}
    :c {:nid :c :version 3}}

   :history
   {:a {1 #{[:a 1]}
        2 #{[:a 1] [:a 2]}
        3 #{[:a 1] [:a 2] [:a 3]}}
    :b {1 #{[:b 1]}
        2 #{[:a 1] [:a 2]
            [:b 1] [:b 2]}
        3 #{[:a 1] [:a 2]
            [:b 1] [:b 2] [:b 3]}}
    :c {1 #{[:c 1]}
        2 #{[:c 1] [:c 2]}
        3 #{[:a 1] [:a 2]
            [:b 1] [:b 2] [:b 3]
            [:c 1] [:c 2] [:c 3]}}}})

(defn simulate [{:keys [send bump init-node] :as fns}]
  (-> (reduce init-node {} [:a :b :c])
      (bump :a)
      (send :a :b)
      (bump :c)
      (bump :a)
      (bump :b)
      (send :b :c)))
