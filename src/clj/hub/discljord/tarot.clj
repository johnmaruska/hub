(ns hub.discljord.tarot
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [hub.discljord.util :as util]
   [hub.util.file :as file]))

(def cards
  (memoize
   #(:cards (file/load-json (io/resource "data/tarot.json")))))

(defn flip-coin []
  (zero? (rand-int 2)))

(defn mix [deck]
  (map #(assoc % :reversed? (flip-coin)) (shuffle deck)))

(defn get-spread [n]
  (take n (mix (cards))))

(defn card->human-readable [card]
  (if (:reversed? card)
    (str "**" (:name card) " - Reversed**" ":\n" (:meaning_rev card))
    (str "**" (:name card) "**"            ":\n" (:meaning_up  card))))

(defn handle-event* [event]
  (let [results (string/join "\n\n" (map card->human-readable (get-spread 3)))]
    (str (mention-user (:author event)) " drew the spread:\n" results)))

(defn handle-event [bot event]
  (util/reply bot event (handle-event* event)))
