(ns hub.minesweeper
  (:require
   [clojure.string :as string]
   [hub.util.grid :as grid]))

(def BOMB 'B)
(defn bomb? [v] (= BOMB v))
(defn safe? [v] (not (bomb? v)))

(defn bomb-coords [height width num-bombs]
  (let [all-coords (for [x (range width)
                         y (range height)
                         :when (not (grid/corner? height width [x y]))]
                     [x y])]
    (take num-bombs (shuffle all-coords))))

(defn add-bombs [board coords]
  (reduce (fn [acc coord]
            (grid/set-coord acc coord BOMB))
          board
          coords))

(defn count-adjacent-bombs [grid coord]
  (->> (grid/get-neighbors grid coord)
       (filter #(bomb? (grid/get-coord grid %)))
       count))

(defn add-counter [grid {:keys [col row value] :as obj}]
  (if (bomb? value)
    grid
    (let [result (count-adjacent-bombs grid [col row])]
      (grid/set-coord grid [col row] result))))

(defn add-counters [grid]
  (reduce add-counter grid (grid/get-coord-objs grid)))

(defn create [height width num-bombs]
  (-> (grid/init height width)
      (add-bombs (bomb-coords height width num-bombs))
      add-counters))
