(ns hub.sketchbook.logical-clocks.causal-history-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [hub.sketchbook.logical-clocks.causal-history :as sut]
   [hub.sketchbook.logical-clocks.util :as util]))

(declare example)

(deftest add-event-test
  (testing "happy path"
    (is (= {:node {:a 2}
            :history {:a {1 #{[:a 1]}
                          2 #{[:a 1] [:a 2]}}}}
           (sut/add-event {:node {:a 1}
                           :history {:a {1 #{[:a 1]}}}}
                          [:a 2]
                          #{[:a 1]})))))

(deftest bump-test
  (testing "happy path"
    (let [state-a1 (sut/init-node {} :a)
          state-a2 {:node {:a 2}
                    :history {:a {1 #{[:a 1]}
                                  2 #{[:a 1] [:a 2]}}}}]
      (is (= state-a2 (sut/bump state-a1 :a))))))

(deftest full-interface
  (let [simulated-example (util/simulate {:send sut/send
                                          :bump sut/bump
                                          :init-node sut/init-node})]
    (testing "Full simulation of Figure 2 matches example structure above"
      (is (= example simulated-example)))
    (testing "Given caused-by? example works"
      (is (sut/caused-by? simulated-example [:c 3] [:a 1])))))

(def example
  {:node {:a 3 :b 3 :c 3}

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
