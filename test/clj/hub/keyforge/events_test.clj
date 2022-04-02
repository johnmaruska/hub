(ns hub.keyforge.events-test
  (:require [hub.keyforge.events :as sut]
            [clojure.test :as t :refer [deftest is testing]]
            [hub.keyforge.card :as card]
            [hub.keyforge.player :as player]
            [hub.keyforge.event-loop :as el]))

(def danova
  {:name "Danova, Port Demolisher"
   :houses ["Brobnar" "Shadows" "Logos"]
   :decklist (take 20 (repeatedly card/escotera))})

(def upbus
  {:name "Upbus, the Madman of the Lighthouse"
   :houses ["Shadows" "Logos" "Dis"]
   :decklist (take 20 (repeatedly card/quixo))})

(comment
  (do
    (set-default-event-sequence! following-event)
    (dispatch-event! [::setup-new-game upbus danova])
    (def END_GAME (process-queue!))))

(def test-player
  (player/make-player danova))

(def test-game
  (sut/setup-new-game {} danova upbus))

(deftest forge-key
  (testing "moves to end game when final key forged"
    (let [result (sut/forge-key (-> test-game
                                    (assoc-in [:player-alpha :forged-keys]
                                              (dec sut/KEYS-TO-WIN))
                                    (assoc-in [:player-alpha :aember]
                                              player/FORGE-COST)))]
      (is (= [:end-game] (::el/dispatch result)))
      (is (= sut/KEYS-TO-WIN (-> result ::el/state :player-alpha :forged-keys)))))
  (testing "increments key but doesn't dispatch when lower keys"
    (let [result (sut/forge-key (-> test-game
                                    (assoc-in [:player-alpha :forged-keys] 0)
                                    (assoc-in [:player-alpha :aember]
                                              player/FORGE-COST)))]
      (is (nil? (::el/dispatch result)))
      (is (= 1 (-> result ::el/state :player-alpha :forged-keys)))))
  (testing "no-op on no key to forge"
    (let [result (sut/forge-key test-game)]
      (is (= test-game (::el/state result))))))

(deftest first-turn-restricted?
  (testing "not restricted after first turn regardless of cards played"
    (is (false? (sut/first-turn-restricted?
                 {:count 2 :cards-played [1 2 3] :cards-discarded [1 2 3]}))))
  (testing "not restricted first turn when no cards played"
    (is (false? (sut/first-turn-restricted?
                 {:count 1 :cards-played [] :cards-discarded []}))))
  (testing "restricted when cards played and first turn"
    (is (true? (sut/first-turn-restricted?
                {:count 1 :cards-played [1] :cards-discarded []})))
    (is (true? (sut/first-turn-restricted?
                {:count 1 :cards-played [] :cards-discarded [1]})))))


(deftest discard-card
  (testing "card removed from hand and placed in discard"
    (let [card (first (-> test-game :player-alpha :hand))
          result (sut/discard-card test-game card)]
      (is (not-any? (partial = card) (-> result :player-alpha :hand)))
      (is (= [card] (-> result :player-alpha :discard))))))
