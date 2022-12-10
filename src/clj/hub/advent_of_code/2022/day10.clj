(ns hub.advent-of-code.2022.day10
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]))

(defn parse [input]
  (->> (slurp (util/reader input))
       string/split-lines
       (map #(string/split % #" "))))


(defn apply-instruction
  "Returns seq of register values, one element for each cycle"
  [x instruction]
  (cond
    (= ["noop"] instruction)
    [x]

    (= "addx" (first instruction))
    (let [v (Integer/parseInt (second instruction))]
      [x (+ x v)])))

(defn state-history [instructions]
  (loop [;; value 1 "during" cycle 0
         ;; value 1 "during" cycle 1, while first instruction takes place.
         history   [1 1]
         remaining instructions]
    (if (empty? remaining)
      history
      (let [register    (last history)
            instruction (first remaining)]
        (recur (concat history (apply-instruction register instruction))
               (rest remaining))))))

(defn look-at-cycle? [[idx itm]]
  (zero? (mod (- idx 20) 40)))

(defn signal-strength [[cycle register]]
  (* cycle register))

(defn part1 []
  (let [history (state-history (parse (util/input 2022 10)))]
    (->> history
         (map-indexed (fn get-idx [idx itm] [idx itm]))
         (filter look-at-cycle?)
         (map signal-strength)
         (apply +))))


(def width 40)
(def height 6)

(defn pixel-lit? [cycle register]
  (<= (dec register) (mod cycle 40) (inc register)))

(defn part2 []
  (let [history (state-history (parse (util/input 2022 10)))]
    (for [h (range height)]
      (string/join
       (for [w (range width)]
         (let [cycle (+ w (* h width))
               register (nth history (inc cycle))]
           (if (pixel-lit? cycle register) "#" ".")))))))
