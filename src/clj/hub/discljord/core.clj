(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]
   [discljord.connections :as c]
   [discljord.events :as e]
   [discljord.messaging :as m]
   [hub.discljord.guess-that-sound :as guess-that-sound]))

(def token (System/getenv "DISCORD_TOKEN"))

(defn start! []
  (let [event-ch (a/chan 100)]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch)
     :message-ch    (m/start-connection! token)}))

(defn stop! [bot]
  (try (m/stop-connection! (:message-ch bot))
       (catch Exception ex nil))
  (try (c/disconnect-bot!  (:connection-ch bot))
       (catch Exception ex nil))
  (try (a/close! (:event-ch bot))
       (catch Exception ex nil)))

(def ignored-events
  #{;;; bot control events
    :connected-all-shards
    :ready
    :guild-create  ; it's connected to a server
    ;;; user action events
    :presence-updated  ; also triggers on game/activity changes like spotify
    :typing-started})

(defn handle-event [bot [event-type event-data]]
  (when (= :message-create event-type)
    (guess-that-sound/handle bot event-data)))

(defn spin-forever! [bot]
  (try
    (loop []
      (handle-event bot (a/<!! (:event-ch bot)))
      (recur))
    (finally (stop! bot))))
