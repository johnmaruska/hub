(ns hub.advent-of-code.2020.day03
  "https://adventofcode.com/2020/day/3"
  (:require
   [hub.advent-of-code.util :as util]
   [hub.util.grid :as grid]))

(def tree \#)

(defn all-indices [increment]
  (map (partial * increment) (range)))

(defn zip
  "Zip an arbitrary number of sequences into a single sequence whose nth elements are the vector of the nth element in each sequence.
  e.g. (zip [1 2 3] [4 5 6]) => '([1 4] [2 5] [3 6])"
  [& xs]
  (apply map (fn [& args] (into [] args)) xs))

(defn wrap-right
  "Wrap the given path from right-edge to left-edge."
  [path width]
  (map (fn [[x y]]
         [(mod x width) y])
       path))

(defn slope-path
  "Find all coordinates encountered by the slope, wrapping at width."
  [slope width]
  (let [[right down] slope
        raw-path     (zip (all-indices right)
                          (all-indices down))]
    (wrap-right raw-path width)))

(defn vals-on-slope [grid slope]
  (let [width (:x (grid/dimensions grid))]
    (for [cell   (slope-path slope width)
          :while (grid/within-bounds? grid cell)]
      (grid/get-coord grid cell))))

(defn count-trees [grid slope]
  (->> (vals-on-slope grid slope)
       (filter #(= tree %))
       count))

(defn part1 [grid]
  (count-trees grid [3 1]))

(defn part2 [grid]
  (let [slopes [[1 1]
                [3 1]
                [5 1]
                [7 1]
                [1 2]]]
    (reduce * (map #(count-trees grid %) slopes))))


(defn run []
  (let [file  (util/input 2020 3)
        input (util/read-file line-seq file)]
    (println "part 1:" (part1 input))
    (println "part 2:" (part2 input))))
