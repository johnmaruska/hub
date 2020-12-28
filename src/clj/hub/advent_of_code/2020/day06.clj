(ns hub.advent-of-code.2020.day06
  (:require
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]))

(defn parse-group [group-str]
  (map #(into #{} %) (string/split group-str #"\r?\n")))

(defn parse [input]
  (vec (map parse-group
            (string/split input #"\r?\n\r?\n"))))

(defn union-group [group]
  (apply set/union group))

(defn intersect-group [group]
  (apply set/intersection group))

(defn sum [results]
  (reduce + (map count results)))

(defn part1 [input]
  (sum (map union-group input)))

(defn part2 [input]
  ;; 1748 is too low
  (sum (map intersect-group input)))

(defn run []
  (let [file  (util/input 2020 6)
        input (parse (slurp (io/resource file)))]
    (println "part 1:" (part1 input))
    (println "part 2:" (part2 input))))
