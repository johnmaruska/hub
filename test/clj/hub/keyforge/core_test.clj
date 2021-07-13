(ns hub.keyforge.core-test
  (:require
   [hub.keyforge.core :as sut]
   [clojure.test :as t :refer [deftest is testing]]))

(def sample-player
  {:identity-card {:houses ["Logos" "Untamed" "Mars"]}
   :battleline [{:name "Valdir" :house "Brobnar"
                 :upgrades [{:name "Protect the Weak" :house "Sanctum"}]}
                {:name "Gungor" :house "Brobnar"}
                {:name "The Terror" :house "Dis"}
                {:name "Bad Penny" :house "Shadows"}]
   :artifacts [{:name "Gauntlet of Command" :house "Brobnar"}]})

(deftest cycle-discard
  (testing "shuffles discard pile into deck"
    (let [shuffled? (atom false)
          player {:deck '()
                  :discard '(0 1 2)}]
      (with-redefs [shuffle (fn [xs]
                              (reset! shuffled? true)
                              xs)]
        (is (= (sut/cycle-discard {:deck '()
                                   :discard '(0 1 2)})
               {:deck '(0 1 2)
                :discard '()}))))))

(deftest draw
  (testing "moves first card from deck to hand"
    (is (= (sut/draw {:deck '(0 1 2) :hand '()})
           {:deck '(1 2) :hand '(0)}))))

(deftest fill-hand
  (testing "draws no cards when hand is full"
    (let [player {:deck '(6 7 8)
                  :hand '(0 1 2 3 4 5)}]
      (is (= player (sut/fill-hand player)))))
  (testing "draws until hand is full from empty"
    (let [player ]
      (is (= (sut/fill-hand {:deck '(0 1 2 3 4 5 6 7 8)
                             :hand '()})
             {:deck '(6 7 8)
              :hand '(0 1 2 3 4 5)})))))

(deftest first-draw
  (testing "draws an additional card"
    (let [player {:deck '(0 1 2 3 4 5 6 7 8 9)}]
      (is (= (inc sut/*HAND-SIZE*)
             (count (:hand (first-draw player))))))))

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
