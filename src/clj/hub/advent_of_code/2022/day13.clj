(ns hub.advent-of-code.2022.day13
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [hub.advent-of-code.util :as utils]))

(defn parse-pair [lines]
  (mapv edn/read-string (string/split-lines lines)))

(defn parse [input]
  (mapv parse-pair (string/split input #"\n\n")))


(defn my-compare [left right]
  (cond
    (and (int? left) (int? right))
    (compare left right)

    (and (int? left) (sequential? right))
    (my-compare [left] right)

    (and (sequential? left) (int? right))
    (my-compare left [right])

    (or (empty? left) (empty? right))
    (compare left right)

    :else
    (let [result (my-compare (first left) (first right))]
      (if (zero? result)
        (my-compare (vec (rest left)) (vec (rest right)))
        result))))

(defn part1 []
  (->> (parse (slurp (str "resources/" (utils/input 2022 13))))
       (keep-indexed (fn [idx pair]
                       (when (neg? (apply my-compare pair))
                         (inc idx))))
       (apply +)))

(defn part2 []
  (let [divider-packets [ [[2]] [[6]] ]]
    (->> (parse (slurp (str "resources/" (utils/input 2022 13))))
         (apply concat)
         (concat divider-packets)
         (sort my-compare)
         (keep-indexed (fn [idx packet]
                         (when ((set divider-packets) packet)
                           (inc idx))))
         (apply *))))
