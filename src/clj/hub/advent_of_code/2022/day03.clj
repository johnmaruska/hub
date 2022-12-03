(ns hub.advent-of-code.2022.day03
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [hub.advent-of-code.util :as util]))

(defn parse [reader]
  (string/split (slurp reader) #"\n"))

(def priority
  (->> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
       (map-indexed (fn [idx x] [x (inc idx)]))
       (into {})))

(defn halve [rucksack]
  (partition (/ (count rucksack) 2) rucksack))

(defn shared-item [group]
  (first (apply set/intersection (map set group))))

(defn part1 []
  (->> (util/read-file parse (util/input 2022 3))
       (map (fn [rucksack] (->> rucksack halve shared-item priority)))
       (apply +)))

(defn part2 []
  (->> (util/read-file parse (util/input 2022 3))
       (partition 3)
       (map (fn [group] (-> group shared-item priority)))
       (apply +)))
