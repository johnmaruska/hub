(ns hub.util.grid)

(defn init
  "Generate a `height` x `width` array, all indices false."
  ([height width]
   (init height width (constantly nil)))
  ([height width value-fn]
   (vec (for [x (range width)]
          (vec (for [y (range height)]
                 (value-fn)))))))

(defn get-coord [board [x y]]
  (nth (nth board y) x))

(defn set-coord [board [x y] value]
  (assoc-in board [y x] value))

(defn within-bounds?
  "Test that coordinate is without bounds of the grid."
  [grid [x y]]
  (try
    (nth (nth grid y) x)
    true
    (catch IndexOutOfBoundsException ex false)))

(defn neighbor-coords
  "Get all coordinates surrounding a given coordinate"
  [[x y]]
  (vec (for [xp [(dec x) x (inc x)]
             yp [(dec y) y (inc y)]
             :when (not (and (= x xp) (= y yp)))]
         [xp yp])))

(defn neighbors [grid coord]
  (filter #(within-bounds? grid %)
          (neighbor-coords coord)))

(defn coord-maps
  "Convert a grid into a list of {:row :col :value} maps.
  Sometimes these are easier to calculate over."
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

(defn dimensions [grid]
  {:y (count grid)
   :x (count (first grid))})

(defn corner?
  ([grid coord]
   (let [{:keys [x y]} (dimensions grid)]
     (corner? y x coord)))
  ([height width [x y]]
   (and (or (= x width)  (zero? x))
        (or (= y height) (zero? y)))))
