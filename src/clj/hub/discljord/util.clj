(ns hub.discljord.util
  (:require
   [clojure.string :as string]
   [discljord.messaging :as m]))

(defn display-seq [xs]
  (->> xs
       (map #(str "`" % "`"))
       (string/join ", ")))

(defn drop-first-word [s]
  (string/join " " (rest (string/split s #" "))))

(defn reply [bot event content]
  (m/create-message! (:message-ch bot) (:channel-id event)
                     :content content))

(defn spoiler [s]
  (str "||" s "||"))
