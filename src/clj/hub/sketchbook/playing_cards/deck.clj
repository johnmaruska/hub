(ns hub.sketchbook.playing-cards.deck
  "Collection of functions for dealing with a deck of standard playing cards.
  This would be a good candidate for a protocol but unless I branch out to other
  types of cards, that's probably overkill."
  (:require [hub.playing-cards.card :as card])
  (:refer-clojure :exclude [peek]))


(def standard-deck
  (vec (for [suit card/suits
             rank card/ranks]
         (card/init suit rank))))

(def jokers [(card/joker "Red") (card/joker "Black")])
(defn add-jokers [cards] (vec (concat cards jokers)))

(defn init
  [& {:keys [shuffled with-jokers]
      :or {shuffled    true
           with-jokers true}
      :as opts}]
  (atom {:options  opts
         :cards    ((if shuffled shuffle identity)
                    (if with-jokers
                      (add-jokers standard-deck)
                      standard-deck))
         :discards []}))

;;; external

(defn remaining [deck] (count (:cards @deck)))
(defn discarded [deck] (count (:discards @deck)))
(defn too-small? [deck n] (< (remaining deck) n))

(defn peek [deck n] (take n (:cards @deck)))

(defn draw! [deck n]
  (let [cards (peek deck n)]
    (swap! deck update :cards #(vec (drop n %)))
    (swap! deck update :discards #(flatten (concat % cards)))
    cards))

(defn shuffle! [deck]
  (swap! deck update :cards #(shuffle (concat % (:discards @deck))))
  (reset! deck (assoc @deck :discards [])))
