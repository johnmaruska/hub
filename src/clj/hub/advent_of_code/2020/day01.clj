(ns hub.advent-of-code.2020.day01
  "https://adventofcode.com/2020/day/1"
  (:require [clojure.java.io :as io]))

(defn read-file [file]
  (with-open [reader (io/reader (io/resource FILE))]
    (vec (map #(Integer/parseInt %) (line-seq reader)))))

(defn find-match [candidates goal]
  (->> candidates
       (filter (fn [candidate]
                 (= goal candidate)))
       first))

(defn ->vec [x]
  (if (vector? x) x [x]))

(defn search
  "Returns head+match "
  [f entries]
  (loop [remaining entries]
    (let [head (first remaining)
          tail (rest remaining)]
      (when (and head (seq tail))  ; dont explode if theyre empty
        (if-let [result (f head tail)]
          (conj (->vec result) head)
          (recur tail))))))

(defn find-pair [entries goal]
  (search (fn [head tail]
            (find-match tail (- goal head)))
          entries))

(defn find-triple [entries goal]
  (search (fn [head tail]
            (find-pair tail (- goal head)))
          entries))

(defn part1 [input goal]
  (apply * (find-pair input goal))) ; => 539851

(defn part2 [input goal]
  (apply * (find-triple input goal)))

(defn run [f]
  (let [file  "advent_of_code/2020/day/1/input.txt"
        goal  2020
        input (read-file FILE)]
    (println "part 1:" (part1 input goal))
    (println "part 2:" (part2 input goal))))
