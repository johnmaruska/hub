(ns hub.dice-test
  (:require
   [hub.dice :as sut]
   [clojure.test :as t :refer [deftest is testing]]))


(deftest roll-all
  (testing "full expression"
    (let [terms [{:sign '+ :n 2 :d 6}   ; min 2, max 12
                 {:sign '- :n 2 :d 1}   ; min 2, max 2
                 {:sign '- :n 4 :d 8}   ; min (* 4 8), max 4 because of sign
                 {:sign '+ :n 1 :d 20}  ; min 1, max 20
                 ]
          results      (sut/roll-all terms)
          expected-min (+  2 (- 2) (- (* 4 8))  1)
          expected-max (+ 12 (- 2) (-       4)  20)]
      (is (= expected-min (:roll-min results)))
      (is (= expected-max (:roll-max results)))
      (is (<= expected-min (:roll-result results) expected-max)))))

(deftest parse
  (testing "A - single die roll"
    (let [expression "1d6"]
      (is (= [{:sign '+ :n 1 :d 6}] (sut/parse expression)))))
  (testing "B - positive modifier"
    (let [expression "1d8 + 3"]
      (is (= [{:sign '+ :n 1 :d 8}
              {:sign '+ :n 3 :d 1}] (sut/parse expression)))))
  (testing "C - negative modifier"
    (let [expression "1d6 - 2"]
      (is (= [{:sign '+ :n 1 :d 6}
              {:sign '+ :n 3 :d 1}]
             (sut/parse expression)))))
  (testing "D - d character is not case sensitive"
    (let [expression "4D6"]
      (is (= [{:sign '+ :n 4 :d 6}] (sut/parse expression)))))
  (testing "E - full expression"
    (let [expression "2d6 - 2 - 4D8 + d20"]
      (is (= [{:sign '+ :n 2 :d 6}
              {:sign '- :n 2 :d 1}
              {:sign '- :n 4 :d 8}
              {:sign '+ :n 1 :d 20}]
             (sut/parse expression))))))
