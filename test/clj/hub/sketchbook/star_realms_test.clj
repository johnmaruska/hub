(ns hub.sketchbook.star-realms-test
  (:require
   [hub.sketchbook.star-realms :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest activate-primary-ability
  (testing "adds trade"
    (let [card   {:primary-ability {:trade 5}}
          player {:trade 0}
          result (sut/activate-primary-ability player card)]
      (is (= 5 (:trade result)))))
  (testing "adds combat"
    (let [card   {:primary-ability {:combat 5}}
          player {:combat 0}
          result (sut/activate-primary-ability player card)]
      (is (= 5 (:combat result)))))
  (testing "adds authority"
    (let [card   {:primary-ability {:authority 5}}
          player {:authority 0}
          result (sut/activate-primary-ability player card)]
      (is (= 5 (:authority result)))))
  (testing "can add multiple types"
    (let [card   {:primary-ability {:authority 3 :combat 5}}
          player {:authority 0 :combat 0}
          result (sut/activate-primary-ability player card)]
      (is (= 3 (:authority result)))
      (is (= 5 (:combat result))))))

(deftest play-card
  (testing "ships go in ship place"
    (let [ship   {:id :card-id :type "Ship"}
          result (sut/play-card {:hand {(:id ship) ship}} :card-id)]
      (is (= {} (:hand result)))
      (is (= {(:id ship) ship} (:ships result)))))
  (testing "bases go in base place"
    (let [base   {:id :card-id :type "Base"}
          result (sut/play-card {:hand {(:id base) base}} :card-id)]
      (is (= {} (:hand result)))
      (is (= {(:id base) base} (:bases result)))))
  (testing "primary ability fires"
    (let [ship   {:id :card-id :type "Ship" :primary-ability {:combat 5}}
          result (sut/play-card {:hand {(:id ship) ship}} :card-id)]
      (is (= {} (:hand result)))
      (is (= 5 (:combat result))))))

(deftest discard-phase
  (testing "clears trade pool"
    (is (= 0 (:trade (sut/discard-phase {:trade 5})))))
  (testing "clears combat pool"
    (is (= 0 (:combat (sut/discard-phase {:combat 5})))))
  (testing "does not clear authority"
    (is (= 50 (:authority (sut/discard-phase {:authority 50})))))
  (testing "clears ships"
    (is (= [1 2 3 4 5 6]
           (:discard (sut/discard-phase {:discard [1 2 3]
                                         :ships   {"four" 4 "five" 5 "six" 6}})))))
  (testing "does not clear bases"
    (is (= {"seven" 7 "eight" 8 "nine" 9}
           (:bases (sut/discard-phase {:bases {"seven" 7 "eight" 8 "nine" 9}}))))))
