(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [discljord.connections :as c]
   [discljord.events :as e]
   [discljord.messaging :as m]))

(def token (System/getenv "DISCORD_TOKEN"))

(defn start! []
  (let [event-ch (a/chan 100)]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch)
     :message-ch    (m/start-connection! token)}))

(defn stop! [bot]
  (m/stop-connection! (:message-ch bot))
  (c/disconnect-bot!  (:connection-ch bot))
  (a/close! (:event-ch bot)))

(defn handle-message-create [data]
  )

(defn handle-event [[event-type event-data]]
  (println "NEW EVENT!")
  (println "Event type:" event-type)
  (println "Event data:" (pr-str event-data)))

(defn spin-forever! [bot]
  (try
    (loop []
      (handle-message (a/<!! (:event-ch bot)))
      (recur))
    (finally (stop! bot))))
