(ns hub.minesweeper
  "Create a Minesweeper gameboard. No mechanics for playing that board."
  (:require [hub.util.grid :as grid]))

(def BOMB 'B)
(defn bomb? [v] (= BOMB v))

(defn bomb-coords
  "Get `num-bombs` bombs and random coordinates within grid `height` x `width` size."
  [height width num-bombs]
  (let [all-coords (for [x (range width)
                         y (range height)
                         :when (not (grid/corner? height width [x y]))]
                     [x y])]
    (take num-bombs (shuffle all-coords))))

(defn add-bombs
  "Given a `board`, place a bomb at each of `coords`."
  [board coords]
  (reduce (fn [acc coord]
            (grid/set-coord acc coord BOMB))
          board
          coords))

(defn adjacent-bombs
  "Return all adjacent coords with bombs."
  [grid coord]
  (filter #(bomb? (grid/get-coord grid %))
          (grid/neighbors grid coord)))

(defn add-counter
  "Add an adjacent-bomb counter to given empty space."
  [grid {:keys [col row value]}]
  (if (not (bomb? value))
    (let [result (count (adjacent-bombs grid [col row]))]
      (grid/set-coord grid [col row] result))
    grid))

(defn add-counters
  "Adds adjacent-bomb counter to empty spaces."
  [grid]
  (reduce add-counter grid (grid/coord-maps grid)))

(defn create
  "Generate a new board for Minesweeper."
  [height width num-bombs]
  (-> (grid/init height width)
      (add-bombs (bomb-coords height width num-bombs))
      add-counters))
