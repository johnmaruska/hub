(ns hub.sketchbook.kata.binary-search
  "Prompt was simply to implement a binary search in a few different ways off the
  top of your head."
  (:require [clojure.test :refer [is]]))


(defn halfway
  ([end]
   (halfway 0 end))
  ([start end]
   (+ start (int (/ (- end start) 2)))))


(defn chop--list-slice
  "Binary chop using list slices and a starting-index offset to remember where we are.

  Each time a section is tested and subsequently narrowed down, actually narrow
  down to only those remaining elements by slicing the array."
  ([target xs]
   (chop--list-slice target xs 0))
  ([target xs start-idx]
   (let [midpoint (halfway (count xs))]
     (cond
       (empty? xs)
       -1

       (= target (nth xs midpoint))
       (+ midpoint start-idx)

       (< target (nth xs midpoint))
       (recur target
              (take midpoint xs)
              start-idx)

       (> target (nth xs midpoint))
       (recur target
              (drop (inc midpoint) xs)
              (+ (inc midpoint) start-idx))))))


(defn chop--bounds
  "Binary chop which calculates new indices based on bounds restrictions.

  Each time an index is tested, set bounds next to it so it's
  disqualified. Iterate over the full array, only tracking these bound indices."
  [target xs]
  (loop [idx         (halfway (count xs))
         lower-bound 0
         upper-bound (count xs)]
    (cond
      (not (contains? xs idx))
      -1

      (= target (nth xs idx))
      idx

      (<= upper-bound lower-bound)
      -1

      (< target (nth xs idx))
      (recur (halfway idx)
             lower-bound (dec idx))

      (> target (nth xs idx))
      (recur (halfway (inc idx) (count xs))
             (inc idx) upper-bound))))


(defn chop--binary-tree
  "Binary chop which constructs the binary tree and simply walks it.

  Constructs a binary tree structured as a triple, (idx, lower, higher), which
  can be easily walked."
  [target xs]
  ;; create a binary tree of indices
  (letfn [(binary-tree* [start end]
            (let [idx (halfway start end)]
              (if (< start end)
                [idx
                 (when (not= idx start)
                   (lazy-seq (binary-tree*     start (dec idx))))
                 (when (not= idx end)
                   (lazy-seq (binary-tree* (inc idx)       end)))]
                [idx nil nil])))]
    ;; [1 3 5] => (1 (0 nil nil) (2 nil nil))

    ;; walk that tree, checking values
    (loop [[idx, next-lower, next-higher] (lazy-seq (binary-tree* 0 (dec (count xs))))]
      (cond
        (empty? xs)
        -1

        (= target (nth xs idx))
        idx

        (and (< target (nth xs idx)) next-lower)
        (recur next-lower)

        (and (> target (nth xs idx)) next-higher)
        (recur next-higher)

        :else
        -1))))

(def chop chop--binary-tree)

;; Tests

(is (= -1 (chop 3 [])))
(is (= -1 (chop 3 [1])))
(is (= 0  (chop 1 [1])))

(is (= 0  (chop 1 [1 3 5])))
(is (= 1  (chop 3 [1 3 5])))
(is (= 2  (chop 5 [1 3 5])))
(is (= -1 (chop 0 [1 3 5])))
(is (= -1 (chop 2 [1 3 5])))

(is (= -1 (chop 4 [1 3 5])))
(is (= -1 (chop 6 [1 3 5])))

(is (= 0  (chop 1 [1 3 5 7])))
(is (= 1  (chop 3 [1 3 5 7])))
(is (= 2  (chop 5 [1 3 5 7])))
(is (= 3  (chop 7 [1 3 5 7])))
(is (= -1 (chop 0 [1 3 5 7])))
(is (= -1 (chop 2 [1 3 5 7])))
(is (= -1 (chop 4 [1 3 5 7])))
(is (= -1 (chop 6 [1 3 5 7])))
(is (= -1 (chop 8 [1 3 5 7])))
