(ns hub.advent-of-code.2022.day05
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as utils]
   [clojure.java.io :as io]))

(defn transpose [m]
  (apply mapv vector m))

(defn parse-state-line [line]
  (map second (partition 4 4 " " line)))

(defn parse-state [state-str]
  (->> (drop-last (string/split state-str #"\n"))
       (map parse-state-line)
       transpose
       (map (fn [stack] (into '() (reverse (remove #(= \space %) stack)))))
       vec))

(defn parse-move [move-str]
  (let [[_ n _ start _ end] (string/split move-str #" ")]
    (map #(Integer/parseInt %) [n start end])))

(defn parse [reader]
  (let [[state moves] (string/split (slurp reader) #"\n\n")]
    {:state (parse-state state)
     :moves (map parse-move (string/split moves #"\n"))}))


(defn n-times [n f x]
  (-> (iterate f x) (nth n)))

(defn top-of-each [stacks] (apply str (map first stacks)))


(defn move-single-crate [state start end]
  (let [crate (peek (get state (dec start)))]
    (-> state
        (update (dec start) pop)
        (update (dec end) conj crate))))

(defn apply-move-by-ones [state [n start end]]
  (n-times n #(move-single-crate % start end) state))

(defn part1 []
  (let [{:keys [state moves]} (parse (utils/reader (utils/input 2022 5)))]
    (->> moves
         (reduce apply-move-by-ones state)
         top-of-each)))

;; these assume stack is a list type, not a vector
(defn peek-n [stack n] (take n stack))
(defn push-n [stack xs] (concat xs stack))
(defn pop-n  [stack n] (drop n stack))

(defn move-crate-group [state [n start end]]
  (let [crates (peek-n (get state (dec start)) n)]
    (-> state
        (update (dec start) pop-n n)
        (update (dec end) push-n crates))))

(defn part2 []
  (let [{:keys [state moves]} (parse (utils/reader (utils/input 2022 5)))]
    (->> moves
         (reduce move-crate-group state)
         top-of-each)))
