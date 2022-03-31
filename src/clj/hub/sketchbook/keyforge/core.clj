(ns hub.sketchbook.keyforge.core
  (:require [hub.sketchbook.card-games :refer [draw-n]]))

(def STANDARD-HAND-SIZE 6)
(def FIRST-DRAW-HAND-SIZE 7)
(def ^:dynamic *HAND-SIZE* STANDARD-HAND-SIZE)
(def FORGE-COST 6)

(def danova
  {:name "Danova, Port Demolisher"
   :houses ["Brobnar" "Shadows" "Logos"]
   :decklist [0 1 2 3 4 5 6 7 8 9]})

(defn make-player [identity-card]
  {:identity-card       identity-card
   :deck                (shuffle (:decklist identity-card))
   :hand                {}
   :discard             '()
   :aember              0
   :battleline          []
   :artifacts           []
   ;; ----
   :forge-cost-modifier 0
   })

(defn fill-hand [player]
  (draw-n player (- *HAND-SIZE* (count (:hand player)))))

(defn first-draw [player]
  (binding [*HAND-SIZE* FIRST-DRAW-HAND-SIZE]
    (fill-hand player)))

;; first player draws 7
;; second player draws 6

(def prep-game
  {:player-alpha (first-draw (make-player danova))
   :player-omega (fill-hand  (make-player danova))})

#_  ;; TODO: where does a mulligan call sit? how do we know if first/second player?
(defn mulligan [player draw-fn]
  (if (:mulliganned? player)
    player
    (binding [*HAND-SIZE* (dec *HAND-SIZE*)]
      (-> (make-player (:identity-card player))
          (assoc :mulliganned? true))
      )))

(defn next-player [game]
  (if (= (:current-player game) :player-alpha)
    :player-omega :player-alpha))

(defn forge-cost [player]
  (+ FORGE-COST (:forge-cost-modifier player)))

(defn forge-key [player]
  (if (<= (forge-cost player) (:aember player))
    (-> player
        (update :aember #(- % (forge-cost player)))
        (update :forged-keys inc))
    player))


(defn controlled-upgrades [player]
  (mapcat :upgrades (:battleline player)))

(defn controlled-cards [player]
  (concat (:battleline player)
          (:artifacts player)
          (controlled-upgrades player)))

(defn available-houses [player]
  (set (concat (:houses (:identity-card player))
               (map :house (controlled-cards player)))))
