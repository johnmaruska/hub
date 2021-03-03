(ns hub.sketchbook.logical-clocks.happened-before-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [hub.sketchbook.logical-clocks.happened-before :as sut]
   [hub.sketchbook.logical-clocks.util :as util]))

(declare example)

(deftest full-interface
  (let [simulated-example (util/simulate {:send sut/send
                                          :bump sut/bump
                                          :init-node sut/init-node})]
    (testing "Full simulation of Figure 2 matches example structure above"
      (is (= example simulated-example)))
    (testing "Given caused-by? example works"
      (is (sut/goes-to? simulated-example [:a 1] [:c 3])))))

(def example
  {:node
   {:a {:nid :a :version 3}
    :b {:nid :b :version 3}
    :c {:nid :c :version 3}}

   :goes-to
   {:a {1 [[:a 2]]
        2 [[:b 2] [:a 3]]
        3 []}
    :b {1 [[:b 2]]
        2 [[:b 3]]
        3 [[:c 3]]}
    :c {1 [[:c 2]]
        2 [[:c 3]]
        3 []}}})
