(ns hub.advent-of-code.2020.day01
  "https://adventofcode.com/2020/day/1"
  (:require [hub.advent-of-code.util :as util]))

(defn parse [reader]
  (map #(Integer/parseInt %) (line-seq reader)))

(defn ->vec [x]
  (if (vector? x) x [x]))

(defn find-match
  [coll v]
  (first (filter #(= v %) coll)))

(defn search
  "Return the result of `f` and the entry it occurs on, for the first entry
  that `f` has a non-nil result.

  Intended to allow crawling a list and 'do something' (f) to each element,
  where that something depends on the remaining entries, i.e. find matches."
  [f entries]
  (loop [remaining entries]
    (let [head (first remaining)
          tail (rest remaining)]
      ;; dont explode if theyre empty
      (when (and head (seq tail))
        (if-let [result (f head tail)]
          (conj (->vec result) head)
          (recur tail))))))

(defn find-pair
  "Find two `entries` which sum together to `goal`."
  [entries goal]
  (search (fn [head tail]
            (find-match tail (- goal head)))
          entries))

(defn find-triple
  "Find three `entries` which sum together to `goal`."
  [entries goal]
  (search (fn [head tail]
            (find-pair tail (- goal head)))
          entries))

(defn part1 [input goal]
  (apply * (find-pair input goal)))

(defn part2 [input goal]
  (apply * (find-triple input goal)))

(defn verify
  "Verify that the code matches provided example."
  []
  (let [example-input [1721 979 366 299 675 1456]]
    (= 514570 (part1 example-input 2020))))

(defn run
  "Run the exercise, printing answers."
  []
  (let [file  (util/input 2020 1)
        goal  2020
        input (util/read-file parse file)]
    (println "part 1:" (part1 input goal))
    (println "part 2:" (part2 input goal))))
