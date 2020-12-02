(ns hub.advent-of-code.util)

(defn input [year day]
  (format "advent_of_code/%s/day/%s/input.txt" year day))

(defn read-file
  "Read and parse an entire file given `parse` fn which accepts an open reader."
  [parse filename]
  (with-open [reader (io/reader (io/resource filename))]
    (vec (parse reader))))
