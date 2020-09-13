(ns hub.world-of-warcraft.the-guide
  "The Guide :tm: is a log of arena matchups and analytic helper fns.
  Currently assumes 2v2 with rogue/warlock."
  (:require
   [hub.util :as util]
   [clojure.string :as str]))

;;;; IO operations

(def filename "world_of_warcraft/the_guide.csv")

(defrecord Enemy [spec class])
(defrecord Match [arena enemy-1 enemy-2 result target-note misc-note])

(defn Enemy->string [enemy]
  (str/join " " [(:spec enemy) (:class enemy)]))
(defn string->Enemy [s]
  (let [[spec & class] (str/split s #" ")]
    (->Enemy spec (str/join " " class))))

(defn parse [csv]
  (map (fn parse-row [row]
         (-> row
             (update :enemy-1 string->Enemy)
             (update :enemy-2 string->Enemy)))
       csv))

;; TODO: delay reading without messing with the atom. memoize, probably
(def match-history (atom nil))
(defn the-guide []
  (or @match-history
      (reset! match-history (parse (util/load-csv filename)))))

(defn format-row [row]
  (str/join "," [(:arena row)
                 (Enemy->string (:enemy-1 row))
                 (Enemy->string (:enemy-2 row))
                 (:result row)
                 (format "\"%s\"" (:target-note row))
                 (format "\"%s\"" (:misc-note row))]))

(defn log-match!
  ([match]
   (swap! match-history #(concat % [match]))
   (let [row (format-row match)]
     (util/write! filename (format-row match) :append true)))
  ([arena enemy-1 enemy-2 result & [target-note misc-note]]
   (log-match! (->Match arena enemy-1 enemy-2 result target-note misc-note))))
