(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]
   [discljord.connections :as c]
   [discljord.events :as e]
   [discljord.messaging :as m]
   [hub.discljord.admin :as admin]
   [hub.discljord.guess-that-sound :as guess-that-sound]
   [hub.discljord.minesweeper :as minesweeper]
   [hub.discljord.role :as role]))

(defn start! []
  (let [event-ch (a/chan 100)
        token    (System/getenv "DISCORD_TOKEN")]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch)
     :message-ch    (m/start-connection! token)}))

(defmacro attempt [& body]
  `(try ~@body (catch Exception ex nil)))

(defn stop! [bot]
  (attempt (m/stop-connection! (:message-ch bot)))
  (attempt (c/disconnect-bot!  (:connection-ch bot)))
  (attempt (a/close! (:event-ch bot))))

(def ignored-events
  #{;;; bot control events
    :connected-all-shards
    :ready
    :guild-create  ; it's connected to a server
    ;;; user action events
    :presence-updated  ; also triggers on game/activity changes like spotify
    :typing-started})

(def message-create-handlers
  [guess-that-sound/handle!
   minesweeper/handle
   admin/handle
   role/handle])

(defn handle-event! [bot [event-type event-data]]
  (when (= :message-create event-type)
    (doseq [h message-create-handlers]
      (h bot event-data))))

(defn spin-forever! [bot]
  (try
    (loop []
      (handle-event! bot (a/<!! (:event-ch bot)))
      (recur))
    (finally (stop! bot))))
