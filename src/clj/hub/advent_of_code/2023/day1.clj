(ns hub.advent-of-code.2023.day1
  (:require [hub.advent-of-code.util :as util]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def real-inputs (util/read-file (comp parse slurp) (util/input 2023 1)))
(def sample-inputs-1
  (parse
   "1abc2
pqr3stu8vwx
a1b2c3d4e5f
treb7uchet"))

(def sample-inputs-2
  (parse
   "two1nine
eightwothree
abcone2threexyz
xtwone3four
4nineeightseven2
zoneight234
7pqrstsixteen"))


(defn parse-digit [s]
  (case s
    "one"   "1"
    "two"   "2"
    "three" "3"
    "four"  "4"
    "five"  "5"
    "six"   "6"
    "seven" "7"
    "eight" "8"
    "nine"  "9"
    s))

(defn calibration-value [digits]
  (Integer/parseInt
   (str (parse-digit (first digits))
        (parse-digit (last digits)))))

(defn parse [contents]
  (string/split contents #"\n"))

(defn part1 [inputs]
  (let [digits (fn [s] (re-seq #"\d" s))]
    (->> inputs
         (map (comp calibration-value digits))
         (reduce +))))

(defn part2 [inputs]
  (let [digits (fn [s]
                 (->> s
                      (re-seq #"(?=(one|two|three|four|five|six|seven|eight|nine|\d))")
                      (map second)))]
    (->> inputs
         (map (comp calibration-value digits))
         (reduce +))))

(part2 real-inputs)
(re-matches #"one|eight" "oneight")
