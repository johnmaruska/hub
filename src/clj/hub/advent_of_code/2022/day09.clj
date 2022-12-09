(ns hub.advent-of-code.2022.day09
  (:require
   [hub.advent-of-code.util :as util]
   [clojure.string :as string]))

(defn parse [input]
  (->> (slurp (util/reader input))
       string/split-lines
       (map #(let [[dir mag] (string/split % #" ")]
               [dir (Integer/parseInt mag)]))))

(defn move-head [[y x] dir]
  (case dir
    "R" [y (inc x)]
    "L" [y (dec x)]
    "D" [(dec y) x]
    "U" [(inc y) x]))

(defn sign-val [x]
  (cond
    (zero? x) 0
    (pos? x)  1
    (neg? x)  -1))

(defn follow [[head-y head-x] [tail-y tail-x]]
  (let [dx (- head-x tail-x)
        dy (- head-y tail-y)]
    (if (and (<= -1 dy 1) (<= -1 dx 1))
      [tail-y tail-x]
      [(+ tail-y (sign-val dy)) (+ tail-x (sign-val dx))])))

(defn init-state [knots]
  {:rope     (repeat knots [0 0])
   :tail-path [[0 0]]})

(defn drag-rope [rope dir]
  (loop [rem-rope (rest rope)
         new-rope [(move-head (first rope) dir)]]
    (if (empty? rem-rope)
      new-rope
      (recur (rest rem-rope)
             (conj new-rope (follow (last new-rope) (first rem-rope)))))))

(defn step [{:keys [rope tail-path]} dir]
  (let [new-rope (drag-rope rope dir)]
    {:rope      new-rope
     :tail-path (conj tail-path (last new-rope))}))

(defn apply-move [state [dir mag]]
  (nth (iterate #(step % dir) state) mag))

(defn part1 []
  (let [moves (parse (util/input 2022 9))]
    (->> (reduce apply-move (init-state 2) moves)
         :tail-path set count)))

(defn part2 []
  (let [moves (parse (util/input 2022 9))]
    (->> (reduce apply-move (init-state 10) moves)
         :tail-path set count)))
