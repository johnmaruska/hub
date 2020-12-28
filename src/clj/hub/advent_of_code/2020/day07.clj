(ns hub.advent-of-code.2020.day07
  (:require
   [clojure.set :as set]
   [instaparse.core :as insta]))

(def parser
  (insta/parser
   "LINE = BAG ' bags contain ' CONTENTS '.';
    CONTENTS = CONTENT (', ' CONTENT)*;
    CONTENT = NUM ' ' BAG ' bags';
    NUM = #'[0-9]+';
    BAG = #'[a-z]+ [a-z]+';"))

(def transform
  {:CONTENT (fn [[_ num] _ [_ bag] _]
              [bag num])
   :CONTENTS (fn [& contents]
               [:CONTENTS (->> contents
                               (filter #(not= ", " %))
                               (into {}))])
   :LINE (fn [[_ bag] _ [_ contents] _]
           {bag contents})})

(defn parse-line [line]
  (insta/transform transform (parser line)))

(defn parse [reader]
  (apply merge (map parse-line (line-seq reader))))

(defn holds [bag-rules container contained]
  (get-in bag-rules [container contained] 0))

(defn can-hold? [bag-rules container contained]
  (not (zero? (holds bag-rules container contained))))

(defn direct-containers [bag-rules content]
  (->> (keys bag-rules)
       (filter #(can-hold? bag-rules % content))
       (into #{})))

(defn all-containers [bag-rules initial-bag]
  (loop [rem-bags     (into #{} (keys (dissoc bag-rules initial-bag)))
         [curr & rem] #{initial-bag}
         all          #{}]
    (let [containers (direct-containers (select-keys bag-rules rem-bags) curr)]
      (if (and (empty? rem) (empty? containers))
        all
        (recur (set/difference rem-bags containers)
               (set/union rem containers)
               (set/union all containers))))))
