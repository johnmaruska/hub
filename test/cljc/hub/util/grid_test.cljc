(ns hub.util.grid-test
  (:require
   [hub.util.grid :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest within-bounds?
  (testing "false on out of bounds -- does not throw exception"
    (is (false? (sut/within-bounds? [[1 2]
                                     [3 4]]
                                    [5 5])))))

(deftest neighbors
  (testing "does not include out of bounds coords"
    (let [results (set (sut/neighbors [[1 2]
                                       [3 4]]
                                      [0 0]))]
      (is (not (results [-1 0])))
      (is (not (results [0 -1])))
      (is (not (results [-1 -1])))))
  (testing "does not include itself"
    (let [results (set (sut/neighbors [[1 2]
                                       [3 4]]
                                      [0 0]))]
      (is (not (results [0 0])))))
  (testing "does include diagonals"
    (let [results (set (sut/neighbors [[1 2]
                                       [3 4]]
                                      [0 0]))]
      (is (results [1 1]))))
  (testing "does include adjacent values"
    (let [results (set (sut/neighbors [[1 2]
                                       [3 4]]
                                      [0 0]))]
      (is (results [1 0]))
      (is (results [0 1])))))

(deftest coord-maps
  (testing "happy path :) does what it's supposed to"
    (is (= [{:row 0 :col 0 :value 1}
            {:row 0 :col 1 :value 2}
            {:row 1 :col 0 :value 3}
            {:row 1 :col 1 :value 4}]
           (sut/coord-maps [[1 2]
                            [3 4]])))))

(deftest corner?
  (testing "true on corners"
    (is (every? #(sut/corner? [[1 2 3]
                               [4 5 6]
                               [7 8 9]] %)
                [[0 0] [0 2] [2 0] [2 2]])))
  (testing "false everywhere else"
    (is (every? #(not (sut/corner? [[1 2 3]
                                    [4 5 6]
                                    [7 8 9]] %))
                [[0 1] [1 0] [1 1] [1 2] [2 1]]))))
