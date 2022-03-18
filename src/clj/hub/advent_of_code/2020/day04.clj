(ns hub.advent-of-code.2020.day04
  "https://adventofcode.com/2020/day/4"
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]
   [clojure.java.io :as io]))

(defn parse-entry [entry]
  (->> entry
       (re-seq #"(\S+):(\S+)")
       (map #(into [] (rest %)))
       (into {})))

(defn parse-raw [input]
  (string/split input #"\r?\n\r?\n"))

(defn parse [input]
  (map parse-entry (parse-raw input)))

;;;; Part 1

(def required-fields #{"byr" "iyr" "eyr" "hgt" "hcl" "ecl" "pid"})

(defn valid-keys? [passport]
  (let [present-fields (into #{} (keys passport))]
    (set/subset? required-fields present-fields)))

(defn part1 [input]
  (count (filter valid-keys? input)))

;;;; Part 2

;;; Could do all these pred fns with a schema checker
;;; want to keep bare clojure though, and spec isn't much better

(defn between [v low high]
  (<= low (Integer/parseInt v) high))

(defn parse-hgt [hgt]
  (when-let [matches (re-find #"(\d+)(cm|in)" hgt)]
    {:q (nth matches 1) :u (nth matches 2)}))

(defn valid-hgt? [hgt]
  (let [{:keys [q u]} (parse-hgt hgt)]
    (cond
      (= "cm" u) (between q 150 193)
      (= "in" u) (between q  59  76))))

(def rules
  {"ecl" #(contains? #{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} %)
   "hcl" #(re-find #"\#[0-9a-f]{6}" %)
   "pid" #(re-find #"^[0-9]{9}$" %)
   "hgt" valid-hgt?
   "byr" #(between % 1920 2002)
   "iyr" #(between % 2010 2020)
   "eyr" #(between % 2020 2030)})

(defn valid-rules? [passport]
  (letfn [(field-met? [[k pred]]
            (when-let [v (get passport k)]
              (pred v)))]
    (every? field-met? rules)))

(defn part2 [input]
  (count (filter valid-rules? input)))

(defn run []
  (let [file  (util/input 2020 4)
        input (parse (slurp (io/resource file)))]
    (println "part 1:" (part1 input))
    (println "part 2:" (part2 input))))
