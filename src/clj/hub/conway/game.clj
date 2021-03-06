(ns hub.conway.game
  "Conway's Game of Life implementation.
  Intended usage includes:
    - `play` to receive a LazySeq of game iterations
    - `alive`, `dead` to abstract representation
    - `start!` to play with a provided display output fn"
  (:require [hub.util.grid :as grid]))

(def alive 1)
(def dead  0)

(defn alive?
  ([cell] (= alive cell))
  ([grid [x y]]
   (alive? (nth (nth grid y) x))))

(defn alive-neighbors [grid coord]
  (filter #(alive? grid %) (grid/neighbors grid coord)))

(defn- step-cell [grid coord]
  (if (alive? grid coord)
    (if (<= 2 (count (alive-neighbors grid coord)) 3) alive dead)
    (if ( = 3 (count (alive-neighbors grid coord)))   alive dead)))

(defn- step-row [grid y row]
  (map-indexed (fn [x _] (step-cell grid [x y])) row))

(defn- step-grid
  "Expects a `grid` whose cells are all either `alive` or `dead`, and whose
  dimensions match those in `m` and `n` via `set-dimensions!`."
  [grid]
  (map-indexed (partial step-row grid) grid))

(def play-round step-grid)
