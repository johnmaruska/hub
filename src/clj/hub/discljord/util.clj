(ns hub.discljord.util
  (:require
   [clojure.string :as string]
   [discljord.messaging :as m]))

(defmacro command
  "Essentially a case statement on a beginning substring. Discord commands are prefixed."
  [content & body]
  {:pre [(even? (count body))]}
  (let [check (fn [substr s] (string/starts-with? s substr))]
    `(condp ~check ~content
       ~@body
       nil)))

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

(defn reply [bot event content]
  (m/create-message! (:message-ch bot) (:channel-id event)
                     :content content))
