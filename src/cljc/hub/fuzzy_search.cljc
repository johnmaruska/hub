(ns hub.fuzzy-search
  "Implementation of fuzzy-search, based on / taken from following links:
  http://makble.com/fuzzy-match-string-like-sublime-in-clojure
  https://gist.github.com/vaughnd/5099299"
  (:require [clojure.string :as string]))

(def MAX-STRING-LENGTH 1000.0)

(defn str-len-distance
  "Normalized length distance between strings. Higher value, higher distance."
  [s1 s2]
  (let [c1    (count s1)
        c2    (count s2)
        max-len (max c1 c2)
        min-len (min c1 c2)]
    (double (/ (- max-len min-len) max-len))))

(defn fuzzy-score
  [query full-str]
  (let [query    (string/lower-case query)
        full-str (string/lower-case full-str)]
    (loop [q     (seq (char-array query))
           s     (seq (char-array full-str))
           score 0]
      (cond
        (empty? q) ; full match on query!
        (+ score
           (- (str-len-distance query full-str))  ;; this sorts by string length on ties
           ;; force full matches to the top by giving max
           (if (<= 0 (.indexOf full-str query)) MAX-STRING-LENGTH 0))

        (empty? s) ; expended full string, not a match
        0

        :else
        (if (= (first q) (first s))
          (recur (rest q) (rest s) (+ 1 score))
          (recur q (rest s) score))))))

(defn fuzzy-search
  [xs query & {:keys [limit] :or {limit 20}}]
  (->> (for [s xs]
         {:data s
          :score (fuzzy-score query s)})
       (filter #(< 0 (:score %)))
       (sort-by :score (comp - compare))
       (take limit)
       (map :data)))
