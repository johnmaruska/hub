(ns hub.discljord.tarot
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [hub.util :refer [parse-json]]
   [hub.util.data-file :refer [reader]]
   [hub.discljord.util :as util]))

(def cards
  (:cards (parse-json (reader "tarot.json"))))

(defn flip-coin []
  (zero? (rand-int 2)))

(defn mix [deck]
  (map (fn [card]
         (assoc card :reversed? (flip-coin)))
       (shuffle deck)))

(defn get-spread [n]
  (take n (mix cards)))

(defn card->human-readable [card]
  (if (:reversed? card)
    (str "**" (:name card) " - Reversed**" ":\n" (:meaning_rev card))
    (str "**" (:name card) "**"            ":\n" (:meaning_up  card))))

(defn handle-event* [event]
  (let [results (string/join "\n\n" (map card->human-readable (get-spread 3)))]
    (str (mention-user (:author event)) " drew the spread:\n" results)))

(defn handle-event [bot event]
  (util/reply bot event (handle-event* event)))
