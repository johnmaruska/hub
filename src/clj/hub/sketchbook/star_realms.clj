(ns hub.sketchbook.star-realms
  (:require
   [hub.sketchbook.card-games :refer [draw-n]]
   [hub.util :refer [uuid]]))

(def viper-card
  {:name    "Viper"
   :type    "Ship"
   :faction "Unaligned"
   :primary {:combat 1} })

(def scout-card
  {:name    "Scout"
   :type    "Ship"
   :faction "Unaligned"
   :primary {:trade 1}})

(def explorer-card
  {:name    "Explorer"
   :type    "Ship"
   :faction "Unaligned"
   :primary {:trade 2}
   :scrap   {:combat 2}
   :cost    2})

(def trade-deck [])

(defn deck [n card]
  (->> (repeat card)
       (map #(assoc % :id (uuid)))
       (take n)))

;;; Setup

(defn init-game [player-ids]
  (reduce (fn init-player [game player-id]
            (assoc-in game [:players player-id]
                      {:id        player-id
                       :discard   []
                       :authority 50
                       :ships     {}
                       :bases     {}
                       :deck      (shuffle (concat (deck 2 viper-card)
                                                   (deck 8 scout-card)))}))
          {:explorers  (deck 16 explorer-card)
           :trade-deck (shuffle trade-deck)
           :scrap-heap []
           :trade-row  [nil nil nil nil nil]}
          player-ids))

;;; Main Phase

;;;; Play a card

(defn activate-primary-ability
  [player card]
  (let [ability (:primary-ability card)]
    (cond-> player
      (    :trade ability) (update     :trade (fnil + 0) (    :trade ability))
      (   :combat ability) (update    :combat (fnil + 0) (   :combat ability))
      (:authority ability) (update :authority (fnil + 0) (:authority ability)))))

(defn play-card [player card-id]
  (let [card (-> player :hand card-id)
        area (if (= "Ship" (:type card)) :ships :bases)]
    (-> player
        (update :hand dissoc card-id)
        (assoc-in [area card-id] card)
        (activate-primary-ability card))))

(defn main-phase [player]
  (loop [player player]
    (let [actions (conj (map #(vec [:play %]) (:hand player))
                        [:pass nil])
          ;; select action arbitrarily
          [action card] (first (shuffle actions))]
      (case action
        :play (recur (play-card player (:id card)))
        :pass player))))

;;; Discard Phase

(defn discard-phase [player]
  (-> player
      (update :discard concat (-> player :ships vals))
      (merge {:trade   0
              :combat  0
              :ships   {}})))

;;; Draw Phase

(defn draw-phase [player]
  (draw-n player 5))


;;; Full turn

(defn full-turn [player]
  (-> player main-phase discard-phase draw-phase))
