(ns hub.advent-of-code.2020.day07
  (:require
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]
   [instaparse.core :as insta]))

(def parser
  (insta/parser
   "LINE = BAG ' bags contain ' CONTENTS '.';
    CONTENTS = CONTENT (', ' CONTENT)* | NOCONTENT;
    NOCONTENT = 'no other bags';
    CONTENT = NUM ' ' BAG ' bag' 's'?;
    NUM = #'[0-9]+';
    BAG = #'[a-z]+ [a-z]+';"))

(def transform
  {:CONTENT (fn [[_ num] _ [_ bag] & _]
              [bag num])
   :NUM      (fn [x]
               [:NUM (Integer/parseInt x)])
   :CONTENTS (fn [& contents]
               [:CONTENTS (->> contents
                               (filter #(not= :NOCONTENT (first %)))
                               (filter #(not= ", " %))
                               (into {}))])
   :LINE (fn [[_ bag] _ [_ contents] _]
           {bag contents})})

(defn parse-line [line]
  (insta/transform transform (parser line)))

(defn parse [lines]
  (apply merge (map parse-line lines)))

(defn holds [bag-rules container contained]
  (get-in bag-rules [container contained] 0))

(defn can-hold? [bag-rules container contained]
  (not (zero? (holds bag-rules container contained))))

(defn direct-containers [bag-rules content]
  (->> (keys bag-rules)
       (filter #(can-hold? bag-rules % content))
       (into #{})))

(defn all-containers [bag-rules initial-bag]
  (count
   (loop [rem-bags     (into #{} (keys (dissoc bag-rules initial-bag)))
          [curr & rem] #{initial-bag}
          all          #{}]
     (let [containers (direct-containers (select-keys bag-rules rem-bags) curr)]
       (if (and (empty? rem) (empty? containers))
         all
         (recur (set/difference rem-bags containers)
                (set/union rem containers)
                (set/union all containers)))))))

(defn total-contained-bags [bag-rules initial-bag]
  (if-let [contents (get bag-rules initial-bag)]
    (reduce + (for [[bag amount] contents]
                (+ amount (* amount (total-contained-bags bag-rules bag)))))
    0))

(defn part1 [input]
  (all-containers input "shiny gold"))

(defn part2 [input]
  (total-contained-bags input "shiny gold"))

(defn run []
  (let [file  (util/input 2020 7)
        input (-> (io/resource file)
                  slurp
                  (string/split #"\n")
                  parse)]
    (println "part 1:" (part1 input))
    (println "part 2:" (part2 input))))
