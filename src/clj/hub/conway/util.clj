(ns hub.conway.util)

(defn get-coords
  "Helper function to make it easier to operate over a grid."
  [grid]
  (->> grid
       (map-indexed (fn [idx row] [idx row]))
       (reduce (fn [acc [row-idx row]]
                 (->> row
                      (map-indexed (fn [col-idx value]
                                     {:row   row-idx
                                      :col   col-idx
                                      :value value}))
                      (concat acc)))
               [])))
