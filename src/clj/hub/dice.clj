(ns hub.dice
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

(s/def ::sign #{'+ '-})
(s/def ::d int?)
(s/def ::n int?)
(s/def ::term
  (s/keys :req [::sign ::d ::n]))

(defn max-value [term]
  (if (= (::sign term) '+) (::d term) 1))

(defn min-value [term]
  (if (= (::sign term) '-) (::d term) 1))

(defn rand-value [term]
  (+ 1 (rand-int (::d term))))

(def sum (partial reduce + 0))

(defn roll-term
  "Perform rolls for an entire term using arbitrary `roll-fn`."
  [roll-fn term]
  {:pre  [(s/valid? ::term term)]
   :post [(s/valid? ::term term)]}
  (let [rolls (repeatedly (::n term) #(roll-fn term))]
    (assoc term ::rolls rolls)))

(defn roll-terms
  ([terms]
   (roll-terms rand-value terms))
  ([roll-fn terms]
   (map (partial roll-term roll-fn) terms)))

(defn eval-term [term]
  (reduce (eval (::sign term)) 0 (::rolls term)))

(defn process-all [terms]
  (letfn [(process [roll-fn]
            (sum (map #(eval-term (roll-term roll-fn %)) terms)))]
    {:roll-result (process rand-value)
     :roll-min    (process min-value)
     :roll-max    (process max-value)}))

(defn parse-int
  "Parse an Integer, optionally providing a default if it cannot be parsed."
  [val & [default-val]]
  (try
    (Integer/parseInt val)
    (catch Exception _
      default-val)))

(defn ->term [groups]
  {:post [(s/valid? ::term %)]}
  (let [[n d] (string/split (nth groups 2) #"[dD]")]
    {::sign (symbol (or (nth groups 1) "+"))
     ::n    (parse-int n 1)
     ::d    (parse-int d 1)}))

(defn parse [expression]
  (->> expression
       (re-seq #"(?: *([-+]) *)?((?:[1-9][0-9]*)?[dD]?(?:[1-9][0-9]*))?")
       (remove (comp empty? first))
       (map ->term)))

(defn main
  "Complete full task from input file to output file."
  [infile outfile]
  (with-open [in (io/reader infile)]
    (->> (line-seq in)
         (map (comp process-all parse))
         (#(with-out-str (json/pprint %)))
         (spit outfile))))
