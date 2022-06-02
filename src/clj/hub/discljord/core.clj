(ns hub.discljord.core
  (:require
   [clojure.core.async :as async]
   [discljord.connections :as connections]
   [discljord.messaging :as messaging]
   [environ.core :refer [env]]
   [hub.discljord.commands :as commands]
   [hub.discljord.util :as util]
   [hub.util :refer [attempt remove-prefix spin-forever swallow-exception]]))

(defn handle-event!
  [bot [event-type event-data]]
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
      (throw ex))))

(defn connect! []
  (let [event-ch (async/chan 100)
        token    (or (env :discord-token)
                     (throw (ex-info "Could not find auth token for discord" {})))
        intents  #{:guilds :guild-messages}]
    {:event-ch      event-ch
     :connection-ch (connections/connect-bot! token event-ch :intents intents)
     :message-ch    (messaging/start-connection! token)}))

(defn disconnect! [bot]
  (attempt (messaging/stop-connection! (:message-ch bot)))
  (attempt (connections/disconnect-bot!  (:connection-ch bot)))
  (attempt (async/close! (:event-ch bot))))

(defmacro spin-until-manual-kill
  "Spins forever, ignoring any exception that does not meet manual-kill criteria"
  [& body]
  `(spin-forever
    (swallow-exception
     (comp not :manual-kill? ex-data)
     ~@body)))

(defmacro with-bot [binding & body]
  `(let [~binding (connect!)]
     (try ~@body (finally (disconnect! ~binding)))))

(defn run-bot! [bot]
  (async/go
    (spin-until-manual-kill
     (if-let [event (async/<! (:event-ch bot))]
       (handle-event! bot event)
       (util/manual-kill!)))))

(defn main [& _args]
  (with-bot discord-bot
    ;; block until bot finishes running
    (async/<!! (run-bot! discord-bot))))
