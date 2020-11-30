(ns hub.world-of-warcraft.the-guide
  "The Guide :tm: is a log of arena matchups and analytic helper fns.
  Currently assumes 2v2 with rogue/warlock."
  (:require
   [hub.util :as util]
   [clojure.string :as str]
   [clojure.set :as set]))

;;;; IO operations

(def twos-guide   "world_of_warcraft/the_guide_2v2.csv")
(def threes-guide "world_of_warcraft/the_guide_3v3.csv")

(defn twos?   [match] (= 2 (count (match :enemies))))
(defn threes? [match] (= 3 (count (match :enemies))))

(defn filename [mode] (if (= :twos mode) twos-guide threes-guide))
(defn mode [match] (if (twos? match) :twos :threes))

(defn ->match
  ([arena enemies result target-note misc-note]
   {:arena       arena
    :enemies     enemies
    :result      result
    :target-note target-note
    :misc-note   misc-note})
  ([arena enemy-1 enemy-2 result target-note misc-note]
   (->match arena [enemy-1 enemy-2] result target-note misc-note))
  ([arena enemy-1 enemy-2 enemy-3 result target-note misc-note]
   (->match arena [enemy-1 enemy-2 enemy-3] result target-note misc-note)))

(defn enemy->string [enemy]
  (str/join " " [(:spec enemy) (:class enemy)]))
(defn string->enemy [s]
  (let [[spec & class] (str/split s #" ")]
    {:spec  spec
     :class (str/join " " class)}))

(def healers
  (->> ["discipline priest"
        "holy priest"
        "holy paladin"
        "mistweaver monk"
        "restoration druid"
        "restoration shaman"]
       (map string->enemy)
       (into #{})))

;;; have to use (into #{} ...) so duplicates dont throw exception
(defn enemy-set [match] (into #{} (match :enemies)))
(defn class-set [match] (into #{} (->> match :enemies (map :class))))
(defn spec-set  [match] (into #{} (->> match :enemies (map :spec))))

(defn enemies [row]
  (->> [(:enemy-1 row) (:enemy-2 row) (:enemy-3 row)]
       (filter identity)
       (map string->enemy)))

(defn parse [csv]
  (map (fn [row] (assoc row :enemies (enemies row))) csv))

(def match-history (atom {}))
(defn the-guide [mode]
  (or (get @match-history mode)
      (swap! match-history assoc mode
             (-> mode filename util/load-csv parse))))

(defn format-row [row]
  (->> [(:arena row)
        (map enemy->string (:enemies row))
        (:result row)
        (format "\"%s\"" (:target-note row))
        (format "\"%s\"" (:misc-note row))]
       (apply concat)
       (str/join ",")))

(defn log-match! [match]
  (swap! match-history #(concat % [match]))
  (let [row  (format-row match)
        file (filename (mode match))]
    (util/append-csv file [row])))

;;;; filter predicates

(defn healer-comp?   [match]
  (some (partial contains? healers) (enemy-set match)))

(defn double-damage? [match]
  (not (healer-comp? match)))

(defn exact-matchup? [match enemies]
  (set/subset? (into #{} enemies) (enemy-set match)))

(defn spec-matchup?  [match enemy-spec]
  (contains? (spec-set match) enemy-spec))

(defn class-matchup? [match enemy-class]
  (contains? (class-set match) enemy-class))

;;;; retrieval

(defn get-match-history
  ([mode]
   (the-guide mode))
  ([mode pred]
   (filter pred (the-guide mode))))
