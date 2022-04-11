(ns hub.fuzzy-search-test
  (:require
   [hub.fuzzy-search :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest str-len-distance
  (testing "maintains expected (< closest furthest) order"
    ;; this isn't testing exact values because i only care about the order.
    (is (= [["rdm" "readme-en.txt"]
            ["rdm" "readme.txt"]
            ["rdmt" "readme.txt"]]
           (sort-by #(apply sut/str-len-distance %) >
                    [["rdmt" "readme.txt"]
                     ["rdm" "readme.txt"]
                     ["rdm" "readme-en.txt"]])))))


(deftest fuzzy-search
  (testing "filters out non-matching entries"
    (is (= ["Bosch DDS180-02 18-Volt Lithium-Ion 1/2-Inch Compact Tough Drill/Driver"]
           (sut/fuzzy-search ["Bosch DDS180-02 18-Volt Lithium-Ion 1/2-Inch Compact Tough Drill/Driver"
                              "Bosch 36618-02 18-Volt Compact-Tough Litheon Drill/Driver"]
                             "dds")))))
