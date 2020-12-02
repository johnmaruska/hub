(ns hub.advent-of-code.2020.day02
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]))

;;;; Shared

(defn parse-line [line]
  (let [[spec letter password] (string/split line #" ")
        ;; intentionally shadow spec because it's just additional parsing
        spec (string/split spec #"-")]
    {:letter   (first letter)
     ;; explicitly pull instead of map/take because it's only two
     :spec     [(Integer/parseInt (first spec))
                (Integer/parseInt (second spec))]
     :password password}))

(defn parse [reader]
  (map parse-line (line-seq reader)))

;;;; Part 1

(defn count-char [password char]
  (count (filter #(= char %) password)))

(defn within-bounds? [{:keys [spec password letter]}]
  (let [[least most] spec
        total (count-char password letter)]
    (boolean (<= least total most))))

(defn part1 [input]
  (count (filter within-bounds? input)))

;;;; Part 2

(defn get-pos
  "Helper so I don't have to remember that positions aren't zero-indexed."
  [v idx]
  (get v (dec idx)))

(defn matches-positions? [{:keys [spec password letter]}]
  (let [matching-positions (filter #(= letter (get-pos password %)) spec)]
    (= 1 (count matching-positions))))

(defn part2 [input]
  (count (filter matches-positions? input)))

;;;; do the thing

(defn run []
  (let [file  (util/input 2020 2)
        input (util/read-file parse file)]
    (println "part 1:" (part1 input))
    (println "part 2:" (part2 input))))
