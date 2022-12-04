(ns hub.advent-of-code.2022.day04
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as utils]))

(defn parse [reader]
  (for [line (line-seq reader)]
    (for [assignment (string/split line #",")]
      (map #(Integer/parseInt %) (string/split assignment #"-")))))

(defn start [assignment] (first assignment))
(defn end [assignment] (second assignment))

(defn fully-contains? [outer inner]
  (and (<= (start outer) (start inner))
       (<= (end inner) (end outer))))

(defn has-fully-contained? [[assignment-a assignment-b]]
  (or (fully-contains? assignment-a assignment-b)
      (fully-contains? assignment-b assignment-a)))

(defn part1 []
  (->> (utils/input 2022 4)
       (utils/read-file parse)
       (filter has-fully-contained?)
       count))


(defn overlaps? [[a b]]
  (or (<= (start b) (end a) (end b))
      (<= (start a) (end b) (end a))))

(defn part2 []
  (->> (utils/input 2022 4)
       (utils/read-file parse)
       (filter overlaps?)
       count))
