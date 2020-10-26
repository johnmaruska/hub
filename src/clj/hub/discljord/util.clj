(ns hub.discljord.util
  (:require
   [clojure.string :as string]
   [discljord.messaging :as m]))

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

(defn reply [bot event content]
  (m/create-message! (:message-ch bot) (:channel-id event)
                     :content content))
