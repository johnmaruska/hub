(ns hub.advent-of-code.2020.day02
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]))

(defn parse-line [line]
  (let [[bounds letter password] (string/split line #" ")
        [least most]             (string/split bounds #"-")]
    {:letter      (first letter)
     :lower-bound (Integer/parseInt least)
     :upper-bound (Integer/parseInt most)
     :password    password}))

(defn parse [reader]
  (map parse-line (line-seq reader)))

(defn count-char [password char]
  (count (filter #(= char %) password)))

(defn valid? [{:keys [lower-bound upper-bound password letter]}]
  (let [total (count-char password letter)]
    (boolean (<= lower-bound total upper-bound))))

(defn part1 [input]
  (count (filter valid? input)))

(defn run []
  (let [file  (util/input 2020 2)
        input (util/read-file parse file)]
    (println "part 1:" (part1 input))))
