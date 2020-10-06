(ns hub.conway.game
  "Conway's Game of Life implementation.
  Intended usage includes:
    - `play` to receive a LazySeq of game iterations
    - `alive`, `dead` to abstract representation
    - `start!` to play with a provided display output fn")

;; count living/dead neighbors
(def alive 1)
(def dead  0)

(def ^:dynamic dimensions)

(defn get-dimensions [grid]
  {:m (count grid)
   :n (count (first grid))})

(defn alive?
  ([cell] (= alive cell))
  ([grid x y]
   (try
     (alive? (nth (nth grid y) x))
     (catch IndexOutOfBoundsException ex
       false))))

(defn- neighbor-coords
  "Get the value of each neighbor in a clockwise-from-12 vec"
  [x y]
  [[     x  (dec y)]  ; above
   [(inc x) (dec y)]  ; above right
   [(inc x)      y]   ; right,
   [(inc x) (inc y)]  ; below right
   [     x  (inc y)]  ; below
   [(dec x) (inc y)]  ; below left
   [(dec x)      y]   ; left
   [(dec x) (dec y)]  ; above left
   ])

(defn count-neighbors [grid x y]
  (->> (neighbor-coords x y)
       (filter (fn [[nx ny]] (alive? grid nx ny)))
       (count)))

;; determine iteration for point
(defn- step-cell [grid x y]
  (if (alive? grid x y)
    (if (<= 2 (count-neighbors grid x y) 3) alive dead)
    (if ( = 3 (count-neighbors grid x y))   alive dead)))

(defn- step-row [grid y row]
  (map-indexed (fn [x _] (step-cell grid x y)) row))

;; perform iteration over entire grid
(defn- step-grid
  "Expects a `grid` whose cells are all either `alive` or `dead`, and whose
  dimensions match those in `m` and `n` via `set-dimensions!`."
  [grid]
  (map-indexed (partial step-row grid) grid))

(defn play [seed]
  (iterate step-grid seed))

(defn start!
  "Start the game, outputing each iteration with given `display` fn

  `display` must accept the grid"
  [display seed-grid iterations]
  ;; bind over dynamic var so we dont recalculate constantly
  (binding [dimensions (get-dimensions seed-grid)]
    (run! #(display % dimensions) (take iterations (play seed-grid)))))


#_ (game/start! terminal/display! seed/toad 5)
