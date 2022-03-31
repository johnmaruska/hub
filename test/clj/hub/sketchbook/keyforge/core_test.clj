(ns hub.sketchbook.keyforge.core-test
  (:require
   [hub.sketchbook.keyforge.core :as sut]
   [clojure.test :as t :refer [deftest is testing]]))

(def sample-player
  {:identity-card {:houses ["Logos" "Untamed" "Mars"]}
   :battleline [{:name "Valdir" :house "Brobnar"
                 :upgrades [{:name "Protect the Weak" :house "Sanctum"}]}
                {:name "Gungor" :house "Brobnar"}
                {:name "The Terror" :house "Dis"}
                {:name "Bad Penny" :house "Shadows"}]
   :artifacts [{:name "Gauntlet of Command" :house "Brobnar"}]})

(deftest fill-hand
  (testing "draws no cards when hand is full"
    (let [player {:deck '({:id 6} {:id 7} {:id 8})
                  :hand {0 {:id 0}
                         1 {:id 1}
                         2 {:id 2}
                         3 {:id 3}
                         4 {:id 4}
                         5 {:id 5}}}]
      (is (= player (sut/fill-hand player)))))
  (testing "draws until hand is full from empty"
    (is (= (sut/fill-hand {:deck '({:id 0} {:id 1} {:id 2} {:id 3} {:id 4}
                                   {:id 5} {:id 6} {:id 7} {:id 8} {:id 9})
                           :hand {}})
           {:deck '({:id 6} {:id 7} {:id 8} {:id 9})
            :hand {0 {:id 0}
                   1 {:id 1}
                   2 {:id 2}
                   3 {:id 3}
                   4 {:id 4}
                   5 {:id 5}}}))))

(deftest first-draw
  (testing "draws an additional card"
    (let [player  {:deck '({:id 0} {:id 1} {:id 2} {:id 3} {:id 4}
                           {:id 5} {:id 6} {:id 7} {:id 8} {:id 9})}]
      (is (= sut/FIRST-DRAW-HAND-SIZE
             (count (:hand (sut/first-draw player))))))))

(deftest available-houses
  (testing "returns the deck's houses from identity-card"
    (is (= #{"Logos" "Untamed" "Mars"}
           (sut/available-houses {:identity-card {:houses ["Logos" "Untamed" "Mars"]}}))))
  (testing "includes houses of controlled cards"
    (is (= #{"Brobnar" "Sanctum" "Star Alliance"}
           (sut/available-houses {:identity-card {:houses []}
                                  :battleline [{:house "Star Alliance"
                                                :upgrades [{:house "Sanctum"}]}]
                                  :artifacts [{:house "Brobnar"}]})))))
