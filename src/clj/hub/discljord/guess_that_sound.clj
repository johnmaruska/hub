(ns hub.discljord.guess-that-sound
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [discljord.messaging :as m]
   [hub.discljord.util :as util]))

;;;; Reply messages

(def canned-reply
  "Pre-made message strings for bot to reply with."
  {:start-game-welcome   "**Time to play `GUESS THAT SOUND`.**"
   :game-already-started "A game has already started."
   :no-game-started      "No guessing game has started"
   :got-guess            "Your guess has been recorded"
   :guess-help           "Whatever mysterious sound is happening in voice, type `!guess` followed by whatever you think the sound is."
   :answer-help          "To conclude the game, type `!answer` followed by what the sound was, and everyone's guesses will be printed. See who gets the closest!"})

;;;; Game state

(def initial-state
  {:guesses []
   :answer  "Not yet provided"})

(defonce game (atom nil))

(defn game-started? [channel-id]
  (not (nil? (get @game channel-id))))

(defn record-guess! [{:keys [author channel-id content]}]
  (let [guess (util/drop-first-word content)]
    (swap! game
           update-in [channel-id :guesses]
           concat [{:author author :guess guess}])))

(defn record-answer! [{:keys [answer channel-id content]}]
  (let [answer (util/drop-first-word content)]
    (swap! game assoc-in [channel-id :answer] answer)))

(defn display [channel-id]
  (let [guess-line  (fn [x] (str (mention-user (:author x))
                                 " guessed "
                                 (:guess x)))
        answer-line (str "Answer: " (-> @game (get channel-id) :answer))
        guess-lines (mapv guess-line (-> @game (get channel-id) :guesses))]
    (string/join "\n" (concat [answer-line] guess-lines))))

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

;;;; User actions

(defn guess! [bot {:keys [channel-id author] :as event}]
  (if (game-started? channel-id)
    (do
      (record-guess! event)
      (util/reply bot event (str (:got-guess canned-reply)
                                 ", " (mention-user author))))
    (util/reply bot event (:no-game-started canned-reply))))

(defn answer! [bot {:keys [channel-id content] :as event}]
  (if (game-started? channel-id)
    (do
      (record-answer! event)
      (util/reply bot event (display channel-id))
      (stop! event))
    (util/reply bot event (:no-game-started canned-reply))))
