(ns hub.discljord.tarot
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [hub.discljord.util :as util]))

(def major-arcana
  (map (fn [x] {:name x})
       ["The Fool"
        "The Magician"
        "The High Priestess"
        "The Empress"
        "The Emperor"
        "The Hierophant"
        "The Lovers"
        "The Chariot"
        "Strength"
        "The Hermit"
        "Wheel of Fortune"
        "Justice"
        "The Hanged Man"
        "Death"
        "Temperance"
        "The Devil"
        "The Tower"
        "The Star"
        "The Moon"
        "The Sun"
        "Judgement"
        "The World"]))

(def minor-arcana
  (for [suit ["Wands" "Cups" "Swords" "Pentacles"]
        rank ["Ace" 2 3 4 5 6 7 8 9 "Page" "Knight" "Queen" "King"]]
    {:suit suit :rank rank}))

(defn card->str [card]
  (str (or (:name card)
           (str (:rank card) " of " (:suit card)))
       (if (:reversed? card) " - Reversed")))

(def full-deck
  (concat major-arcana minor-arcana))

(defn flip-coin []
  (zero? (rand-int 2)))

(defn mix [deck]
  (map (fn [card]
         (assoc card :reversed? (flip-coin)))
       (shuffle deck)))

(defn get-spread [n]
  (->> (mix full-deck)
       (take n)
       (map card->str)))

(defn handle-event [bot event]
  (util/reply bot event
              (str (mention-user (:author event)) " drew the spread:\n"
                   (string/join "\n" (get-spread 3)))))
