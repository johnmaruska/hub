(ns hub.conway.output.terminal
  (:require
   [hub.conway.game :as game]
   [hub.util.grid :as grid]))

(def cell->string
  {game/alive "X"
   game/dead  "."})

(def spacing 2)
(defn- ->string [grid]
  (->> grid
       (map (fn row->string [row]
              (->> row
                   (map cell->string)
                   (interpose (apply str (repeat spacing " ")))
                   (apply str))))
       (interpose "\n")
       (apply str)))

(defn- total-chars-per-line
  "Calculate total characters per line required to display grid of dimension `n`.
  Calculation assumes each line is the dimension with `spacing` characters
  interposed."
  [n]
  (+ n (* spacing (- n 1))))

(defn- make-bookend
  "Create string for preceding/following grid display for visual separation on
  repeated prints."
  [n]
  (->> (repeat "=")
       (take (total-chars-per-line n))
       (apply str)))

(defn clear []
  (print (str (char 27) "[2J")) ; clear screen
  (print (str (char 27) "[;H"))) ; move cursor to the top left corner of the screen

(defn display
  " Prints to the console the given grid with `=` top/bottom, `X` alive and `.`
  dead cells.
  =======
  X  .  .
  X  .  X
  .  .  X
  ======="
  [grid & [dimensions]]
  (clear)
  (let [dimensions (or dimensions (grid/dimensions grid))
        bookend    (make-bookend (:x dimensions))]
    (println (apply str [bookend "\n" (->string grid) "\n" bookend]))))

(defn animate
  "Continuously display grid updates, animating changes.
  Blocking call that spins infinitely. Used either in isolation or from a thread."
  [seed-grid update-fn delay-ms]
  (let [dimensions (grid/dimensions seed-grid)]
    (run! (fn [x]
            (display x dimensions)
            (Thread/sleep delay-ms))
          (iterate update-fn seed-grid))))
