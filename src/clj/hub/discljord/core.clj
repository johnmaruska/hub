(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [discljord.connections :as c]
   [discljord.messaging :as m]
   [environ.core :refer [env]]
   [hub.discljord.commands :as commands]
   [hub.discljord.util :as util]
   [hub.util :refer [remove-prefix swallow-exception]]))

(defn handle-event!
  ([bot]
   (handle-event! bot (a/<!! (:event-ch bot))))
  ([bot [event-type event-data]]
   (try
     (when (= :message-create event-type)
       (doseq [matching-prefix (commands/matching-prefixes event-data)]
         (let [handler    (get commands/prefix matching-prefix)
               event-data (update event-data :content remove-prefix matching-prefix)]
           ;; TODO: if multiple handlers allowed, modify here
           (handler bot event-data))))
     (catch Exception ex
       (when-not (:manual-kill? (ex-data ex))
         (util/reply bot event-data "Could not handle command. See logs."))
       (throw ex)))))

(defn start! []
  (let [event-ch (a/chan 100)
        token    (or (env :discord-token)
                     (throw (ex-info "Could not find auth token for discord" {})))
        intents  #{:guilds :guild-messages}]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch :intents intents)
     :message-ch    (m/start-connection! token)}))

(defmacro attempt [& body]
  `(try ~@body (catch Exception ex# nil)))

(defn stop! [bot]
  (attempt (m/stop-connection! (:message-ch bot)))
  (attempt (c/disconnect-bot!  (:connection-ch bot)))
  (attempt (a/close! (:event-ch bot))))

(defmacro spin-forever [& body]
  `(loop [] ~@body (recur)))

(def manual-kill? (comp :manual-kill? ex-data))

(defmacro spin-until-manual-kill
  "Spins forever, ignoring any exception that does not meet manual-kill criteria"
  [& body]
  `(spin-forever
    (swallow-exception (comp not manual-kill?)
      ~@body)))

(defn main [& _args]
  (let [discord-bot (start!)]
    (try
      (spin-until-manual-kill
       (handle-event! discord-bot))
      (finally
        (stop! discord-bot)))))
