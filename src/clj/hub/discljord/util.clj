(ns hub.discljord.util
  (:require [discljord.messaging :as m]))

(defn reply
  "Have `bot` send a message in the same channel as trigger `event`."
  [bot event content]
  (m/create-message! (:message-ch bot) (:channel-id event)
                     :content content))

(defn manual-kill! []
  (throw (ex-info "ManualKill" {:manual-kill? true})))


(defn spoiler [s]
  (str "||" s "||"))

(defn ticks [s]
  (str "`" s "`"))
