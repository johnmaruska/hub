(ns hub.advent-of-code.2022.day02
  (:require
   [clojure.string :as string]
   [hub.advent-of-code.util :as util]))

(defn parse [reader]
  (->> (line-seq reader)
       (map #(map keyword (string/split % #" ")))))

(def example
  [[:A :Y]
   [:B :X]
   [:C :Z]])  ; 15

(def shape-points    {:rock 1 :paper 2 :scissors 3})
(def outcome-points  {:loss 0 :tie 3 :win 6})
(def decode-opponent {:A :rock :B :paper :C :scissors})
(def decode-player   {:X :rock :Y :paper :Z :scissors})

;; (:rock beats) => :scissors
;; not (beats :rock) => :scissors which is wrong
(def beats
  {:rock     :scissors
   :scissors :paper
   :paper    :rock})

(defn outcome [opponent player]
  (cond
    (= opponent player)         :tie
    (= (opponent beats) player) :loss
    (= (player beats) opponent) :win))

(defn score-round [shape outcome]
  (+ (outcome-points outcome) (shape-points shape)))

(defn score-moves [[opponent player]]
  (score-round player (outcome opponent player)))

(defn decode-round-by-moves [[opponent-val player-val]]
  [(decode-opponent opponent-val) (decode-player player-val)])

(defn part1 []
  (->> (util/read-file parse (util/input 2022 2))
       (map decode-round-by-moves)
       (map score-moves)
       (reduce + 0)))



(def decode-outcome  {:X :loss :Y :tie :Z :win})
(def beaten-by (clojure.set/map-invert beats))

(defn decode-round-by-outcome [[opponent-val outcome-val]]
  [(decode-opponent opponent-val) (decode-outcome outcome-val)])

(defn player-move [opponent-move outcome]
  (case outcome
    :win  (opponent-move beaten-by)
    :tie  opponent-move
    :loss (opponent-move beats)))

(defn by-outcome->by-moves [[opponent-move outcome]]
  [opponent-move (player-move opponent-move outcome)])

(defn score-round-by-outcome [round-by-outcome]
  (-> round-by-outcome
      decode-round-by-outcome
      by-outcome->by-moves
      score-moves))

(defn part2 []
  (->> (util/read-file parse (util/input 2022 2))
       (map score-round-by-outcome)
       (reduce + 0)))
