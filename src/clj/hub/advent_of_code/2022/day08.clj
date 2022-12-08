(ns hub.advent-of-code.2022.day08
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]
   [clojure.java.io :as io]))

(defn tree [height]
  {:height height :visible? false})

(defn parse-grid [file]
  (map (fn [line]
         (map #(tree (Integer/parseInt %)) (string/split line #"")))
       (string/split-lines (slurp file))))

(defn flag-row [row & [include-equal?]]
  (loop [remaining    row
         flagged      []
         highest-seen -1]
    (cond
      (empty? remaining)
      flagged

      ((if include-equal? <= <) highest-seen (:height (first remaining)))
      (recur (rest remaining)
             (conj flagged (assoc (first remaining) :visible? true))
             (:height (first remaining)))

      :else
      (recur (rest remaining) (conj flagged (first remaining)) highest-seen))))

(defn flag-rows [grid]
  (for [row grid]
    (reverse (flag-row (reverse (flag-row row))))))

(defn transpose [m]
  (apply mapv vector m))

(defn flag-columns [grid]
  (transpose (flag-rows (transpose grid))))

(defn flag-grid [grid]
  (-> grid flag-rows flag-columns))

(defn count-visible [grid]
  (->> grid
       (apply concat)
       (filter :visible?)
       count))

(defn part1 []
  (let [input (io/file "resources" (util/input 2022 8))]
    (-> input
        parse-grid
        flag-grid
        count-visible)))

(defn select-col [grid col-idx]
  (map (fn [row] (nth row col-idx)) grid))

(defn get-tree-at [grid [row-idx col-idx]]
  (-> grid (nth row-idx) (nth col-idx)))

(defn up-from [grid [row-idx col-idx]]
  (reverse (take row-idx (select-col grid col-idx))))
(defn down-from [grid [row-idx col-idx]]
  (drop (inc row-idx) (select-col grid col-idx)))

(defn right-from [grid [row-idx col-idx]]
  (drop (inc col-idx) (nth grid row-idx)))
(defn left-from [grid [row-idx col-idx]]
  (reverse (take col-idx (nth grid row-idx))))

(defn visible-from-treehouse [tree-house trees]
  (letfn [(lower? [tree]
            (< (:height tree) (:height tree-house)))]
    (let [blocking-tree   (first (drop-while lower? trees))
          unblocked-trees (take-while lower? trees)]
      ;; TODO: only visible trees?
      (remove nil? (concat unblocked-trees [blocking-tree])))))

(defn out-from [grid coord]
  {:up    (up-from grid coord)
   :left  (left-from grid coord)
   :down  (down-from grid coord)
   :right (right-from grid coord)})

(defn scenic-score [grid coord]
  (->> (out-from grid coord)
       vals
       (map #(visible-from-treehouse (get-tree-at grid coord) %))
       (map count)
       (reduce *)))

(defn part2 []
  (let [grid   (parse-grid (io/file "resources" (util/input 2022 8)))
        coords (for [row-idx (range (count (first grid)))
                     col-idx (range (count grid))]
                 [row-idx col-idx])]
    (->> coords
         (map (partial scenic-score grid))
         (apply max))))
