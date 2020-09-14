(ns hub.world-of-warcraft.the-guide
  "The Guide :tm: is a log of arena matchups and analytic helper fns.
  Currently assumes 2v2 with rogue/warlock."
  (:require
   [hub.util :as util]
   [clojure.string :as str]
   [clojure.set :as set]))

;;;; IO operations

(def filename "world_of_warcraft/the_guide.csv")

(defn ->match [arena enemy-1 enemy-2 result target-note misc-note]
  {:arena       arena
   :enemy-1     enemy-1     :enemy-2   enemy-2
   :result      result
   :target-note target-note :misc-note misc-note})

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
(defn enemy-set [match] (into #{} [(:enemy-1 match) (:enemy-2 match)]))
(defn class-set [match] (into #{} [(get-in match [:enemy-1 :class])
                                   (get-in match [:enemy-2 :class])]))
(defn spec-set  [match] (into #{} [(get-in match [:enemy-1 :spec])
                                   (get-in match [:enemy-2 :spec])]))

(defn parse [csv]
  (map (fn parse-row [row]
         (-> row
             (update :enemy-1 string->enemy)
             (update :enemy-2 string->enemy)))
       csv))

;; TODO: delay reading without messing with the atom. memoize, probably
(def match-history (atom nil))
(defn the-guide []
  (or @match-history
      (reset! match-history (parse (util/load-csv filename)))))

(defn format-row [row]
  (str/join "," [(:arena row)
                 (enemy->string (:enemy-1 row))
                 (enemy->string (:enemy-2 row))
                 (:result row)
                 (format "\"%s\"" (:target-note row))
                 (format "\"%s\"" (:misc-note row))]))

(defn log-match!
  ([match]
   (swap! match-history #(concat % [match]))
   (let [row (format-row match)]
     (util/write! filename (format-row match) :append true)))
  ([arena enemy-1 enemy-2 result & [target-note misc-note]]
   (log-match! (->match arena enemy-1 enemy-2 result target-note misc-note))))

;;;; filter predicates

(defn healer-comp? [match]
  (some (partial contains? healers) (enemy-set match)))

(defn double-damage? [match]
  (not (healer-comp? match)))

(defn exact-matchup? [enemies match]
  (set/subset? (into #{} enemies) (enemy-set match)))

(defn spec-matchup? [enemy-spec match]
  (contains? (spec-set match) enemy-spec))

(defn class-matchup? [enemy-class match]
  (contains? (class-set match) enemy-class))

;;;; retrieval

(defn get-match-history [& [pred]]
  (filter (or pred identity) (the-guide)))
