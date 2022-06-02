(ns hub.dice-test
  (:require
   [hub.dice :as sut]
   [clojure.test :as t :refer [deftest is testing]]))

(deftest process-all
  (testing "full expression"
    (let [terms [{::sut/sign '+ ::sut/n 2 ::sut/d 6}   ; min 2, max 12
                 {::sut/sign '- ::sut/n 2 ::sut/d 1}   ; min 2, max 2
                 {::sut/sign '- ::sut/n 4 ::sut/d 8}   ; min (* 4 8), max 4 because of sign
                 {::sut/sign '+ ::sut/n 1 ::sut/d 20}  ; min 1, max 20
                 ]
          results      (sut/process-all terms)
          expected-min (+  2 (- 2) (- (* 4 8))  1)
          expected-max (+ 12 (- 2) (-       4)  20)]
      (is (= expected-min (:roll-min results)))
      (is (= expected-max (:roll-max results)))
      (is (<= expected-min (:roll-result results) expected-max)))))

(deftest parse
  (testing "A - single die roll"
    (let [expression "1d6"]
      (is (= [{::sut/sign '+ ::sut/n 1 ::sut/d 6}] (sut/parse expression)))))
  (testing "B - positive modifier"
    (let [expression "1d8 + 3"]
      (is (= [{::sut/sign '+ ::sut/n 1 ::sut/d 8}
              {::sut/sign '+ ::sut/n 3 ::sut/d 1}] (sut/parse expression)))))
  (testing "C - negative modifier"
    (let [expression "1d6 - 2"]
      (is (= [{::sut/sign '+ ::sut/n 1 ::sut/d 6}
              {::sut/sign '- ::sut/n 2 ::sut/d 1}]
             (sut/parse expression)))))
  (testing "D - d character is not case sensitive"
    (let [expression "4D6"]
      (is (= [{::sut/sign '+ ::sut/n 4 ::sut/d 6}] (sut/parse expression)))))
  (testing "E - full expression"
    (let [expression "2d6 - 2 - 4D8 + d20"]
      (is (= [{::sut/sign '+ ::sut/n 2 ::sut/d 6}
              {::sut/sign '- ::sut/n 2 ::sut/d 1}
              {::sut/sign '- ::sut/n 4 ::sut/d 8}
              {::sut/sign '+ ::sut/n 1 ::sut/d 20}]
             (sut/parse expression))))))
