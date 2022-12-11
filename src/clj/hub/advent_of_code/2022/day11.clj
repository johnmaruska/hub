(ns hub.advent-of-code.2022.day11
  (:require [clojure.string :as string]
            [hub.advent-of-code.util :as util]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def input-regex
  #"Monkey ([0-9]):
  Starting items: ([\d, ]+)
  Operation: new = (\d+|old) ([\+\*]) (\d+|old)
  Test: divisible by (\d+)
    If true: throw to monkey (\d+)
    If false: throw to monkey (\d+)")

(defn ->int [x] (edn/read-string x))

(defn parse-monkey [section]
  (let [numbers        (re-find input-regex section)
        monkey-idx     (->int (nth numbers 1))
        starting-items (map ->int (string/split (nth numbers 2) #", "))
        operand-1      (nth numbers 3)
        operation      (nth numbers 4)
        operand-2      (nth numbers 5)
        divisible-by   (->int (nth numbers 6))
        true-clause    (->int (nth numbers 7))
        false-clause   (->int (nth numbers 8))]
    {:idx        monkey-idx
     :n-inspects 0
     :items      starting-items
     :divisor    divisible-by
     :test       (fn throw-to [worry-level]
                   (if (zero? (mod worry-level divisible-by))
                     true-clause false-clause))
     :operation  (fn ->operation [old]
                   (let [arg1 (if (= "old" operand-1) old (->int operand-1))
                         op   (if (= "+" operation) + *)
                         arg2 (if (= "old" operand-2) old (->int operand-2))]
                     (op arg1 arg2)))}))

(defn parse-monkeys [input]
  (->> (string/split input #"\n\n")
       (mapv parse-monkey)))

(def ^:dynamic *worry* identity)

(defn handle-item [state {:keys [items operation test idx]}]
  (let [;; inspect item
        item                (first items)
        inspect-worry-level (operation item)
        ;; get bored
        bored-worry-level   (*worry* inspect-worry-level)
        next-monkey         (test bored-worry-level)]
    (-> state
        (update-in [idx :n-inspects] inc)
        (update-in [idx :items] rest)
        (update-in [next-monkey :items] concat [bored-worry-level]))))

(defn handle-items [state-0 monkey-idx]
  (loop [state state-0]
    (if (empty? (get-in state [monkey-idx :items]))
      state
      (recur (handle-item state (nth state monkey-idx))))))

(defn handle-round [state]
  (reduce handle-items state (range (count state))))

(defn handle-n-rounds [n state]
  (nth (iterate handle-round state) n))

(defn monkey-business [state]
  (->> state (map :n-inspects) (sort-by -) (take 2) (apply *)))

(defn part1 []
  (with-redefs [*worry* (fn [x] (quot x 3))]
    (->> (str "resources/" (util/input 2022 11))
         io/reader slurp
         parse-monkeys
         (handle-n-rounds 20)
         monkey-business)))

(defn least-common-multiple [state]
  (->> state (map :divisor) (apply *)))

(defn part2 []
  (let [state (->> (str "resources/" (util/input 2022 11))
                   io/reader slurp
                   parse-monkeys)
        lcm   (least-common-multiple state)]
    (with-redefs [*worry* (fn [x] (mod x lcm))]
      (->> state
           (handle-n-rounds 10000)
           monkey-business))))
