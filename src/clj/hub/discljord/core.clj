(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [discljord.connections :as c]
   [discljord.formatting :refer [mention-user]]
   [discljord.messaging :as m]
   [hub.discljord.commands :as commands]
   [hub.discljord.util :as util]
   [hub.util]))

(defn sign-off [event-data]
  (str "Will I dream, " (mention-user (:author event-data)) "?"))

(defn remove-prefix [event-data prefix]
  (update event-data :content
          #(hub.util/remove-prefix % prefix)))

(defn handle-event!
  ([bot]
   (handle-event! bot (a/<!! (:event-ch bot))))
  ([bot [event-type event-data]]
   (try
     (when (= :message-create event-type)
       (doseq [matching-prefix (commands/matching-prefixes event-data)]
         (let [handler    (get commands/prefix matching-prefix)
               event-data (remove-prefix event-data matching-prefix)]
           ;; TODO: if multiple handlers allowed, modify here
           (handler bot event-data))))
     (catch Exception ex
       (if (:manual-kill? (ex-data ex))
         (util/reply bot event-data (sign-off event-data))
         (util/reply bot event-data "Could not handle command. See logs."))
       (throw ex)))))

(defn start! []
  (let [event-ch (a/chan 100)
        token    (System/getenv "DISCORD_TOKEN")]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch)
     :message-ch    (m/start-connection! token)}))

(defmacro attempt [& body]
  `(try ~@body (catch Exception ex# nil)))

(defn stop! [bot]
  (attempt (m/stop-connection! (:message-ch bot)))
  (attempt (c/disconnect-bot!  (:connection-ch bot)))
  (attempt (a/close! (:event-ch bot))))
