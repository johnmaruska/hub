(ns hub.conway.seed
  (:require
   [hub.conway.game :as game]
   [hub.conway.util :as util]))

;; initializing

(defn all-dead [m n]
  (vec (take m (repeat (vec (take n (repeat game/dead)))))))

(defn overlay-cell
  "Overlay a value into an nd-array. Maintains alive cells, overwrites dead cells.

  returns unchanged grid if coordinates out of bounds, following behavior of `game/alive?`"
  [grid x y val]
  (if (game/alive? grid x y)
    grid
    (assoc-in grid [y x] val)))

(defn overlay
  "place `seed` in `grid` pinned to `x0` `y0`, populating each cell in the
  `grid` which is populated in the `seed`."
  ([grid seed] (overlay grid seed 0 0))
  ([grid seed x0 y0]
   (reduce (fn [acc coord]
             (overlay-cell acc
                           (+ x0 (:col coord))
                           (+ y0 (:row coord))
                           (:value coord)))
           grid
           (util/get-coords seed))))

;; Still lifes

(def block
  [[1 1]
   [1 1]])

(def bee-hive
  [[0 1 1 0]
   [1 0 0 1]
   [0 1 1 0]])

;;;; Oscillators

(def toad
  [[0 0 1 0]
   [1 0 0 1]
   [1 0 0 1]
   [0 1 0 0]])

(def beacon
  [[1 1 0 0]
   [1 1 0 0]
   [0 0 1 1]
   [0 0 1 1]])

;;;; Spaceships

(def glider
  [[1 0 0]
   [0 1 1]
   [1 1 0]])
