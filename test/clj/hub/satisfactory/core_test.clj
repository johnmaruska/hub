(ns hub.satisfactory.core-test
  (:require
   [hub.satisfactory.core :as sut]
   [clojure.test :as t :refer [deftest is testing]]))

(deftest parse-map-column
  (testing "parses a map"
    (is (= {"crude-oil" 2 "plastic" 2}
           (sut/parse-map-column "crude-oil=2 plastic=2"))))
  (testing "parses a number"
    (is (= 13 (sut/parse-map-column "13")))))

(deftest raw-materials
  (testing "grabs correct numbers for computer"
    (is (= {"copper-ore" 49
            "crude-oil"  87
            "iron-ore"   13}
           (sut/raw-materials "computer")))))
