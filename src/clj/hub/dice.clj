(ns hub.dice
  (:require
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

(defn max-roll [d] d)
(defn min-roll [d] 1)
(defn rand-roll [d]
  (+ 1 (rand-int d)))

(defn roll-term
  "Perform rolls for an entire term using arbitrary `roll-fn`."
  [roll-fn {:keys [n d sign]}]
  (->> (repeatedly n #(roll-fn d))
       (reduce (eval sign) 0)))

(defn roll-terms
  [roll-fn terms]
  (->> (map (partial roll-term roll-fn) terms)
       (reduce + 0)))

(defn roll-all [terms]
  {:roll-result (roll-terms rand-roll terms)
   :roll-min    (roll-terms min-roll terms)
   :roll-max    (roll-terms max-roll terms)})

(defn parse-int
  "Parse an Integer, optionally providing a default if it cannot be parsed."
  [val & [default-val]]
  (try
    (Integer/parseInt val)
    (catch Exception _
      default-val)))

(defn ->term [groups]
  (let [[n d] (string/split (nth groups 2) #"[dD]")]
    {:sign (symbol (or (nth groups 1) "+"))
     :n    (parse-int n 1)
     :d    (parse-int d 1)}))

(defn parse [expression]
  (->> expression
       (re-seq #"(?: *([-+]) *)?((?:[1-9][0-9]*)?[dD]?(?:[1-9][0-9]*))?")
       (remove (comp empty? first))
       (map ->term)))


(defn task
  "Complete full task from input file to output file."
  [infile outfile]
  (with-open [in (io/reader infile)]
    (->> (line-seq in)
         (map (comp roll-all parse))
         (#(with-out-str (json/pprint %)))
         (spit outfile))))

(task "test.txt" "test.out")
