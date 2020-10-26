(ns hub.discljord.guess-that-sound
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [discljord.messaging :as m]
   [hub.discljord.util :as util]))

;;;; Reply messages

(def canned-reply
  {:start-game-welcome   "**Time to play `GUESS THAT SOUND`.**"
   :game-already-started "A game has already started."
   :no-game-started      "No guessing game has started"
   :guess-help           "Whatever mysterious sound is happening in voice, type `!guess` followed by whatever you think the sound is."
   :answer-help          "To conclude the game, type `!answer` followed by what the sound was, and everyone's guesses will be printed. See who gets the closest!"})


;;;; Game state

(def initial-state
  {:guesses []
   :answer  "Not yet provided"})

(defonce game (atom {}))

(defn game-started? [channel-id]
  (not (nil? (get @game channel-id))))

(defn display [channel-id]
  (let [guess-line  (fn [x] (str (mention-user (:author x))
                                 " guessed "
                                 (:guess x)))
        answer-line (str "Answer: " (-> @game (get channel-id) :answer))
        guess-lines (mapv guess-line (-> @game (get channel-id) :guesses))]
    (string/join "\n" (concat [answer-line] guess-lines))))


;;;; User actions

(defn guess! [bot event]
  (let [guess  (util/drop-first-word (:content event))
        append (fn [a g] (update-in a [(:channel-id event) :guesses] conj g))]
    (swap! game append {:author (:author event) :guess guess})))

(defn answer! [bot event]
  (let [channel-id (:channel-id event)]
    (swap! game assoc-in [channel-id :answer]
           (util/drop-first-word (:content event)))
    (util/reply bot event (display channel-id))))


;;;; Game lifecycle

(defn start-reply [bot event header]
  (let [{:keys [guess-help answer-help]} canned-reply
        lines [header guess-help answer-help]]
    (util/reply bot event (string/join "\n" lines))))

(defn init-game! [bot event]
  (start-reply bot event (:start-game-welcome canned-reply))
  (swap! game assoc (:channel-id event) initial-state))

(defn game-already-started [bot event]
  (start-reply bot event (:game-already-started canned-reply)))

(defn start! [bot event]
  (if (game-started? (:channel-id event))
    (game-already-started bot event)
    (init-game! bot event)))

(defn stop! [event]
  (swap! game assoc (:channel-id event) nil))

;;;; create-message event handler

(defn handle [bot event]
  (letfn [(when-game-started [action]
            (if (game-started? (:channel-id event))
              (action bot event)
              (util/reply bot event (:no-game-started canned-reply))))]
    (condp (fn [substr s] (string/starts-with? s substr)) (:content event)
      "!play guess-that-sound" (start! bot event)
      "!guess"  (when-game-started guess!)
      "!answer" (do (when-game-started answer!) (stop! event))
      nil)))
