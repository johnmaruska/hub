(ns hub.advent-of-code.2022.day05
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [hub.advent-of-code.util :as utils]))

(defn transpose [m]
  (apply mapv vector m))

(defn parse-state-line [line]
  (map second (partition 4 4 " " line)))

(defn ->stack [column]
  (->> column
       (remove #(= \space %))
       reverse
       (into '())))

(defn parse-state [state-str]
  (->> (string/split state-str #"\n")
       drop-last
       (map parse-state-line)
       transpose
       (map ->stack)
       vec))

(defn parse-move [move-str]
  (let [[_ n _ start _ end] (string/split move-str #" ")]
    (map #(Integer/parseInt %) [n start end])))

(defn parse [reader]
  (let [[state moves] (string/split (slurp reader) #"\n\n")]
    {:state (parse-state state)
     :moves (map parse-move (string/split moves #"\n"))}))


;; these assume stack is a list type, not a vector
(defn peek-n [stack n] (take n stack))
(defn push-n [stack xs] (concat xs stack))
(defn pop-n  [stack n] (drop n stack))
(defn top-str [stacks]
  (apply str (map first stacks)))

(defn move-crate-group [state [n start end]]
  (let [crates (peek-n (get state (dec start)) n)]
    (-> state
        (update (dec start) pop-n n)
        (update (dec end) push-n crates))))


(defn apply-move-by-ones [state [n start end]]
  (nth (iterate #(move-crate-group % [1 start end]) state) n))

(defn part1 []
  (let [{:keys [state moves]} (parse (utils/reader (utils/input 2022 5)))]
    (top-str (reduce apply-move-by-ones state moves))))


(defn part2 []
  (let [{:keys [state moves]} (parse (utils/reader (utils/input 2022 5)))]
    (top-str (reduce move-crate-group state moves))))
