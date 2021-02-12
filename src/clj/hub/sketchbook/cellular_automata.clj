(ns hub.sketchbook.cellular-automata)

;; 1. On each tick, a cell tries to be the same color that the cell above it was last tick.
;; 2. On each tick, a cell tries NOT to be the same color that the cell below it was last tick.
;; 3. If they ever conflict, Rule 1 takes precedence over Rule 2.
;; 4. If none of these rules apply, a cell stays as it is.

(defn above [world cell-idx] (get world (dec cell-idx)))
(defn below [world cell-idx] (get world (inc cell-idx)))
(defn curr  [world cell-idx] (get world cell-idx))

(defn rule1 [world cell-idx]
  (when (<= 1 cell-idx)
    (above world cell-idx)))

(defn rule2 [world cell-idx]
  (when (< cell-idx (count world))
    (below world cell-idx)))

(defn new-cell [world cell-idx]
  (or (rule1 world cell-idx)
      (rule2 world cell-idx)
      (curr  world cell-idx)))

(defn step [world]
  (map-indexed (fn [idx _]
                 (new-cell world idx))
               world))
