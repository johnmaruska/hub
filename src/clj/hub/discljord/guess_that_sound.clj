(ns hub.discljord.guess-that-sound
  (:require [clojure.string :as string]))

;;;; NOT YET IMPLEMENTED

(def mention identity)
(def reply println)


;;;; Reply messages

(def canned-reply
  {:start-game-welcome   "*Time to play `GUESS THAT SOUND`.*"
   :game-already-started "A game has already started."
   :no-game-started      "No guessing game has started"
   :guess-help           "Whatever mysterious sound is happening in voice, type `!guess` followed by whatever you think the sound is."
   :answer-help          "To conclude the game, type `!answer` followed by what the sound was, and everyone's guesses will be printed. See who gets the closest!"})

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

;;;; Game state

(def initial-state
  {:guesses []
   :answer  "Not yet provided"})

;; TODO: make one per channel not one total
(defonce game (atom nil))

(defn game-started? [] (not (nil? @game)))

(defn display []
  (let [guess-line  (fn [x] (str (mention (:author x))
                                 " guessed "
                                 (:guess x)))
        answer-line (str "Answer: " (:answer @game))
        guess-lines (mapv guess-line (:guesses @game))]
    (string/join "\n" (concat [answer-line] guess-lines))))


;;;; User actions

(defn guess!
  "Track a guess"
  [bot event]
  (let [guess  (drop-first-word (:content event))
        append (fn [a g] (update a :guesses conj g))]
    (swap! game append {:author (:author event) :guess guess})))

(defn answer! [bot event]
  (swap! game assoc :answer (drop-first-word (:content event)))
  (reply (display)))


;;;; Game lifecycle

(defn start-reply [bot header]
  (let [{:keys [guess-help answer-help]} canned-reply
        lines [header guess-help answer-help]]
    (reply (string/join "\n" lines))))

(defn init-game! [bot]
  (start-reply bot (:start-game-welcome canned-reply))
  (reset! game initial-state))

(defn game-already-started [bot]
  (start-reply bot (:game-already-started canned-reply)))

(defn start! [bot]
  (if (game-started?)
    (game-already-started bot)
    (init-game! bot)))

(defn stop! [] (reset! game nil))

;;;; create-message event handler

(defmacro when-game-started [& body]
  `(if (game-started?)
     (do ~@body)
     (reply (:no-game-started canned-reply))))

(defn handler [bot event]
  (condp string/starts-with? (:content event)
    "!play guess-that-sound"
    (start! bot)

    "!guess"
    (when-game-started (guess! bot event))

    "!answer"
    (when-game-started (answer! bot event))))
