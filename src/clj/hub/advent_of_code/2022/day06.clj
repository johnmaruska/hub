(ns hub.advent-of-code.2022.day06
  (:require
   [hub.advent-of-code.util :as utils]
   [clojure.string :as string]))

(defn parse [filename]
  (string/trim (slurp (utils/reader filename))))

(defn start-marker-idx [window-size data-stream]
  (->> data-stream
       (partition window-size 1)
       ;; (+ window-size idx) is _end_ of _marker_ idx
       (map-indexed (fn [idx itm] [(+ window-size idx) itm]))
       (filter (fn [[idx itm]] (apply distinct? itm)))
       first first))

(defn part1 []
  (start-marker-idx 4 (parse (utils/input 2022 6))))

(defn part2 []
  (start-marker-idx 14 (parse (utils/input 2022 6))))
