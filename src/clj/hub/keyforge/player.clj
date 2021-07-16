(ns hub.keyforge.player)

(def ^:dynamic *HAND-SIZE* 6)
(def FORGE-COST 6)

(defn make-player [identity-card]
  {:identity-card       identity-card
   :deck                (shuffle (:decklist identity-card))
   :hand                '()
   :discard             '()
   :aember              0
   :battleline          []
   :artifacts           []
   ;; ----
   :forge-cost-modifier 0})

(defn cycle-discard [player]
  (if (empty? (:deck player))
    (assoc player
           :deck (shuffle (:discard player))
           :discard '())
    player))

(defn draw [player]
  (-> player
      (update :hand #(conj % (first (:deck player))))
      (update :deck rest)
      cycle-discard))

(defn fill-hand-to [player n]
  (let [cards-to-draw (max 0 (- n (count (:hand player))))]
    (nth (iterate draw player) cards-to-draw)))

(defn fill-hand [player]
  (fill-hand-to player *HAND-SIZE*))

(defn first-draw [player]
  (fill-hand-to player (inc *HAND-SIZE*)))

#_  ;; TODO: where does a mulligan call sit? how do we know if first/second player?
(defn mulligan [player draw-fn]
  (if (:mulliganned? player)
    player
    (binding [*HAND-SIZE* (dec *HAND-SIZE*)]
      (-> (make-player (:identity-card player))
          (assoc :mulliganned? true))
      )))

(defn forge-cost [player]
  (+ FORGE-COST (or (:forge-cost-modifier player) 0)))

(defn forge-key [player]
  (def PLAYER player)
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

(defn ready-all [player]
  (-> player
      (update :battleline #(map (fn [card] (assoc card :ready? true)) %))
      (update :artifacts  #(map (fn [card] (assoc card :ready? true)) %))))
