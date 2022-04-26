(ns hub.sketchbook.keyforge.events
  (:require
   [hub.sketchbook.keyforge.player :as player]
   [hub.sketchbook.keyforge.event-loop :as el :refer [defevent]]
   [hub.sketchbook.keyforge.card :as card]
   [hub.util :as util]))

(def KEYS-TO-WIN 3)

;; TODO: we'll deal with user interaction later
(defn user-interaction [prompt choices]
  (println prompt ":" choices)
  (first choices))

(defn current-player [game]
  (get game (:current-player game)))

;;; game setup

(defevent setup-new-game [_ alpha-identity omega-identity]
  (-> {:turn           {:count 1}
       :current-player :player-alpha
       :player-alpha   (player/make-player alpha-identity)
       :player-omega   (player/make-player omega-identity)}
      (update :player-alpha player/first-draw)
      (update :player-omega player/fill-hand)))

;;; take turn

(defevent forge-key [game]
  (let [result (update game (:current-player game) player/forge-key)]
    (if (= KEYS-TO-WIN (-> result current-player :forged-keys))
      {::el/state result ::el/dispatch [:end-game]}
      {::el/state result})))

(defevent choose-house [game]
  (let [choices (player/available-houses (current-player game))]
    (assoc-in game [:turn :house]
              (user-interaction "Activate a House" choices))))

(defn first-turn-restricted? [turn]
  (and (= 1 (:count turn))
       (<= 1 (+ (-> turn :cards-played count)
                (-> turn :cards-discarded count)))))

(defevent take-turn [game]
  (let [current-house? (fn [card]
                         (= (-> game :turn :house) (:house @card)))
        playable       (when-not (first-turn-restricted? (:turn game))
                         (map (fn [x] {:card x :actions #{:play :discard}})
                              (filter current-house? (-> game current-player :hand))))
        creatures      (map (fn [x] {:card x :actions #{:reap :fight :action}})
                            (filter current-house? (-> game current-player :battleline)))
        choices        (concat playable creatures [:end-turn])
        #_#_choices    (concat (map (fn [x] {:card x :actions #{:play :discard}})
                                    (filter current-house? (-> game current-player :hand)))
                               (map (fn [x] {:card x :actions #{:reap :fight :action}})
                                    (filter current-house? (-> game current-player :battleline)))
                               (map (fn [x] {:card x :actions #{:action}})
                                    (filter current-house? (-> game :current-play :artifacts)))
                               (map (fn [x] {:card x :actions #{:omni}})
                                    (filter (comp :omni deref) (-> game :current-play :artifacts)))
                               [:end-turn])
        choice         (user-interaction "What do?" choices)]
    (if (= :end-turn choice)
      {::el/state game ::el/dispatch ::end-turn}
      {::el/state game ::el/dispatch [::interact-card choice]})))

;;; START ---- Play a single card --------------------------------------

(defevent interact-card [game {:keys [card actions]}]
  (let [action (user-interaction "What interaction with this card?" actions)
        event  ({:play    ::play-card
                 :omni    ::omni-card
                 :action  ::action-card
                 :discard ::discard-card
                 :reap    ::reap-card
                 :fight   ::fight-card} action)]
    {::el/state game ::el/dispatch [event card]}))

(defevent discard-card [game card]
  (-> game
      (update-in [(:current-player game) :hand] card/remove-card card)
      (update-in [(:current-player game) :discard] conj card)))

(defevent reap-card [game card]
  (card/exhaust! card)
  (let [reap-effect (:or (:reap @card) identity)]
    (-> game
        (update-in [(:current-player game) :aember] inc)
        reap-effect)))

(defevent fight-card [game _card]
  ;; TODO: implement
  game)

(defevent play-card [game card]
  (let [play-effect (or (:play @card) identity)]
    {::el/state    (-> game
                       (update-in [:turn :cards-played] conj card)
                       (update (:current-player game) :hand card/remove-card card)
                       play-effect)
     ::el/dispatch (case (:type @card)
                     :artifact [::place-artifact card]
                     :creature [::place-creature card]
                     :action   [::place-action   card])}))

(defn available-placements [game card]
  (let [total-creatures (count (-> game current-player :battlefield))
        left-flank      0
        right-flank     total-creatures]
    (if (:deploy @card)  ; TODO(CARD-EFFECT)
      (range (inc total-creatures))
      #{left-flank right-flank})))

(defevent place-creature [game card]
  (swap! card assoc :ready? (:enters-ready @card))
  (if (empty? (-> game current-player :battlefield))
    (assoc-in game [(:current-player game) :battlefield] [card])
    (let [choice (user-interaction "Place creature on which flank?"
                                   (available-placements game card))]
      (update-in game [(:current-player game) :battlefield]
                 #(util/insert-at % choice card)))))

(defevent place-artifact [game card]
  (update-in game [(:current-player game) :artifacts] conj card))

(defevent place-action [game card]
  (update-in game [(:current-player game) :discard] conj card))

;;; END ---- Play a single card ----------------------------------------


(defevent ready-cards [game]
  (update game (:current-player game) player/ready-all))

(defevent draw-cards [game]
  (update game (:current-player game) player/fill-hand))

(defn other-player [player-kw]
  (if (= :player-alpha player-kw)
    :player-omega :player-alpha))

(defevent switch-turns [game]
  (update game :current-player other-player))

(def game-setup
  {::setup-new-game         ::player-alpha-mulligan
   ::player-alpha-mulligan  ::player-omega-mulligan
   ::player-omega-mulligan  :turn/init})

(def turn-sequence
  {:turn/init     ::forge-key
   ::forge-key    ::choose-house
   ::choose-house ::take-turn
   ;; no default for take-turn
   ;; taken-turn choose between Card or End
   ;; chosen card, choose Play/Discard, or Use
   ;; using card, choose beween Action/Omni, Reap/Fight/Actionan
   ::play-card    ::take-turn
   ::discard      ::take-turn
   ::use-card     ::take-turn
   ::end-turn     ::ready-cards
   ::ready-cards  ::draw-cards
   ::draw-cards   ::switch-turns
   ;; ::switch-turns :turn/init
   })

(def following-event
  (merge game-setup turn-sequence))
