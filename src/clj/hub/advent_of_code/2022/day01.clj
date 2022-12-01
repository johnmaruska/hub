(ns hub.advent-of-code.2022.day01
  (:require
   [hub.advent-of-code.util :as util]
   [clojure.java.io :as io]
   [clojure.string :as string]))

(defn parse [reader]
  (map (fn parse-group [s]
         (map #(Integer/parseInt %) (string/split s #"\n")))
       (string/split (slurp reader) #"\n\n")))

(defn sum [xs] (reduce + 0 xs))

(defn part1 []
  (apply max (map sum (util/read-file parse (util/input 2022 1)))))

(defn part2 []
  (sum (take 3 (reverse (sort (map sum (util/read-file parse (util/input 2022 1))))))))
