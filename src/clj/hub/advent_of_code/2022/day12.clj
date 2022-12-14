(ns hub.advent-of-code.2022.day12
  (:require
   [clojure.data.priority-map :refer [priority-map]]
   [clojure.string :as string]
   [hub.advent-of-code.util :as utils]))

(def dirs
  {:left  [0 -1]
   :right [0 1]
   :up    [-1 0]
   :down  [1 0]})

(defn y [[row-idx col-idx]] row-idx)
(defn x [[row-idx col-idx]] col-idx)

(defn all-coords [grid]
  (->> (for [row (range (count grid))]
         (for [col (range (count (first grid)))]
           [row col]))
       (apply concat)))

(defn check-coord [grid [row-idx col-idx]]
  (-> grid (nth row-idx) (nth col-idx)))

(defn vector+ [& vs]
  (apply mapv + vs))

(defn within-bounds? [grid coord]
  (and (<= 0 (y coord) (dec (count grid)))
       (<= 0 (x coord) (dec (count (first grid))))))

(defn manhattan-distance [coord-a coord-b]
  (let [[y1 x1] coord-a
        [y2 x2] coord-b]
    (+ (Math/abs (- y2 y1)) (Math/abs (- x2 x1)))))


(defn parse [input]
  (map vec (string/split-lines input)))

(defn elevation [grid coord]
  (let [elev-char (check-coord grid coord)]
    (cond
      (= \E elev-char) \z
      (= \S elev-char) \a
      :else elev-char)))

(defn elevation-delta [grid start-coord end-coord]
  (- (int (elevation grid end-coord))
     (int (elevation grid start-coord))))

(defn moves [grid coord]
  (->> (vals dirs)
       (map (partial vector+ coord))
       (filter (partial within-bounds? grid))))


(defn starting-point [grid]
  (->> (all-coords grid)
       (filter #(= \S (check-coord grid %)))
       first))

(defn mountain-top [grid]
  (->> (all-coords grid)
       (filter #(= \E (check-coord grid %)))
       first))

(defn node [coord path]
  {:coord coord :path path})

(defn bfs [grid start-coord meets-goal? get-moves-fn]
  (loop [visited #{}
         queue   (conj clojure.lang.PersistentQueue/EMPTY
                       {:coord start-coord :path []})]
    (if (empty? queue)
      nil #_(throw (ex-info "Expended queue!" {}))
      (let [{:keys [coord path]} (peek queue)
            rest-queue           (pop queue)]
        (cond
          (meets-goal? coord)
          path

          (visited coord)
          (recur visited rest-queue)

          :else
          (do
            (when (zero? (mod (count visited) 2))
              (println "visited" (count visited) "nodes"))
            (recur (conj visited coord)
                   (->> (get-moves-fn coord)
                        (map #(node % (conj path coord)))
                        (into rest-queue)))))))))


(defn moves-hiking-up [grid coord]
  (->> (moves grid coord)
       ;; can only step up one at a time
       (remove #(< 1 (elevation-delta grid coord %)))))

(defn part1 []
  (let [grid         (parse (slurp (str "resources/" (utils/input 2022 12))))
        start-coord  (starting-point grid)]
    (count (bfs grid start-coord
                #(= \E (check-coord grid %))
                (partial moves-hiking-up grid)))))


(defn moves-hiking-down [grid coord]
  (->> (moves grid coord)
       ;; can only step DOWN one at a time
       (remove #(< 1 (elevation-delta grid % coord)))))

(defn part2 []
  (let [grid        (parse (slurp (str "resources/" (utils/example 2022 12 1))))
        start-coord (mountain-top grid)]
    (count (bfs grid start-coord
                #(= \a (check-coord grid %))
                (partial moves-hiking-down grid)))))
