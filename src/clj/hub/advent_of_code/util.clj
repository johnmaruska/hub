(ns hub.advent-of-code.util
  (:require [clojure.java.io :as io]))

(defn input [year day]
  (format "advent_of_code/%s/day/%s/input.txt" year day))

(defn example [year day n]
  (format "advent_of_code/%s/day/%s/example%s.txt" year day n))

(defn read-file
  "Read and parse an entire file given `parse` fn which accepts an open reader."
  [parse filename]
  ;; use str resources instead of io/resources so we don't have to reload classpath
  ;; to get new files
  (with-open [reader (io/reader (str "resources/" filename))]
    (vec (parse reader))))
