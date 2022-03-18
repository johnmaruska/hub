(ns hub.card-games-test
  (:require
   [hub.card-games :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest cycle-discard
  (testing "replaces empty deck with discard pile deck"
    (let [player {:deck    '()
                  :discard '({:id 0} {:id 1} {:id 2})}
          result (sut/cycle-discard player)]
      (is (empty? (:discard result)))
      (is (= #{{:id 0} {:id 1} {:id 2}} (into #{} (:deck result))))))
  (testing "no-ops when deck is not empty"
    (let [player {:deck    '({:id 0})
                  :discard '({:id 1} {:id 2})}
          result (sut/cycle-discard player)]
      (is (= player result))))
  (testing "shuffles cards (via shuffle fn)"
    (let [shuffled? (atom false)]
      (with-redefs [shuffle (fn [xs]
                              (reset! shuffled? true)
                              xs)]
        (sut/cycle-discard {:deck '() :discard '({:id 0} {:id 1} {:id 2})})
        (is @shuffled?)))))

(deftest draw
  (testing "moves first card from deck to hand"
    (is (= (sut/draw {:deck '({:id 0} {:id 1} {:id 2}) :hand {}})
           {:deck '({:id 1} {:id 2}) :hand {0 {:id 0}}}))))

(deftest draw-n
  (testing "draw-n cycles discard when deck is empty"
    (let [player  {:deck    []
                   :discard [{:id :discarded} {:id :not-drawn}]
                   :hand    {}}
          results (sut/draw-n player 1)]
      (is (= 1 (count (:deck results))))
      (is (= 1 (count (:hand results))))
      (is (not= (:deck results) (vals (:hand results))))))
  (testing "draw-n ... draws n"
    (let [player  {:deck [{:id 1} {:id 2} {:id 3}]}
          results (sut/draw-n player 2)]
      (is (= [{:id 3}] (:deck results)))
      (is (= {1 {:id 1} 2 {:id 2}} (:hand results))))))
