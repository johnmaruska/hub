(ns hub.discljord.util
  (:require
   [clojure.string :as string]
   [discljord.messaging :as m]))

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

(defn reply
  "Have `bot` send a message in the same channel as trigger `event`."
  [bot event content]
  (m/create-message! (:message-ch bot) (:channel-id event)
                     :content content))

(defn spoiler [s]
  (str "||" s "||"))
