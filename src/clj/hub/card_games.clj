(ns hub.card-games
  "Most card games share basic mechanics. This is their home.

  Most functions operate as if they're updating an entity. They typically refer
  to a modified version of their first argument.
  e.g. (update player draw-n 5) is valid.")

(defn cycle-discard
  [player]
  (if (empty? (:deck player))
    (assoc player
           :deck (shuffle (:discard player))
           :discard [])
    player))

(defn draw [player]
  (let [;; Not all card games will cycle a discard pile.
        ;; I think most with discards that don't cycle refer to it by another name?
        ;; When that case is encountered, separate at that point
        player (cycle-discard player)
        card   (first (:deck player))]
    (-> player
        (assoc :deck (rest (:deck player)))
        (assoc-in [:hand (:id card)] card))))

(defn draw-n [player n]
  (nth (iterate draw player) n))
