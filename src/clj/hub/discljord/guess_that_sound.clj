(ns hub.discljord.guess-that-sound
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer mention-user]
   [discljord.messaging :as m]))

;;;; Reply messages

(def canned-reply
  {:start-game-welcome   "*Time to play `GUESS THAT SOUND`.*"
   :game-already-started "A game has already started."
   :no-game-started      "No guessing game has started"
   :guess-help           "Whatever mysterious sound is happening in voice, type `!guess` followed by whatever you think the sound is."
   :answer-help          "To conclude the game, type `!answer` followed by what the sound was, and everyone's guesses will be printed. See who gets the closest!"})

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

(defn reply [bot event contents]
  (m/create-message! (:message-ch bot)
                     (:channel-id event)
                     contents))

;;;; Game state

(def initial-state
  {:guesses []
   :answer  "Not yet provided"})

;; TODO: make one per channel not one total
(defonce game (atom nil))

(defn game-started? [] (not (nil? @game)))

(defn display []
  (let [guess-line  (fn [x] (str (mention-user (:author x))
                                 " guessed "
                                 (:guess x)))
        answer-line (str "Answer: " (:answer @game))
        guess-lines (mapv guess-line (:guesses @game))]
    (string/join "\n" (concat [answer-line] guess-lines))))


;;;; User actions

(defn guess! [bot event]
  (let [guess  (drop-first-word (:content event))
        append (fn [a g] (update a :guesses conj g))]
    (swap! game append {:author (:author event) :guess guess})))

(defn answer! [bot event]
  (swap! game assoc :answer (drop-first-word (:content event)))
  (reply bot event (display)))


;;;; Game lifecycle

(defn start-reply [bot event header]
  (let [{:keys [guess-help answer-help]} canned-reply
        lines [header guess-help answer-help]]
    (reply bot event (string/join "\n" lines))))

(defn init-game! [bot event]
  (start-reply bot event (:start-game-welcome canned-reply))
  (reset! game initial-state))

(defn game-already-started [bot event]
  (start-reply bot event (:game-already-started canned-reply)))

(defn start! [bot event]
  (if (game-started?)
    (game-already-started bot event)
    (init-game! bot event)))

(defn stop! [] (reset! game nil))

;;;; create-message event handler

(defn handler [bot event]
  (letfn [(when-game-started [action]
            (if (game-started?)
              (action bot event)
              (reply bot event (:no-game-started canned-reply))))]
    (condp string/starts-with? (:content event)
      "!play guess-that-sound" (start! bot)
      "!guess"  (when-game-started guess!)
      "!answer" (when-game-started answer!))))
