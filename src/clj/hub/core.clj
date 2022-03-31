(ns hub.core
  (:require
   [hub.cli.id3-fix :as id3]
   [hub.conway :as conway]
   [hub.dice :as dice]
   [hub.spotify :as spotify]
   [hub.discljord.core :as discord]
   [hub.server :as server]
   [hub.util :refer [swallow-exception]]
   [mount.core :as mount :refer [defstate]]
   [clojure.tools.logging :as log])
  (:gen-class))

(defstate webserver
  :start (server/start!)
  :stop  (.stop webserver))

(defmacro spin-forever [& body]
  `(loop [] ~@body (recur)))

(def manual-kill? (comp :manual-kill? ex-data))

(defmacro spin-until-manual-kill [& body]
  `(swallow-exception manual-kill?
     (spin-forever
      (swallow-exception (comp not manual-kill?)
        ~@body))))

(defn discord-run! []
  (let [discord-bot (discord/start!)]
    (try
      (spin-until-manual-kill
       (discord/handle-event! discord-bot))
      (finally
        (discord/stop! discord-bot)))))

;; TODO: proper monorepo doesn't just switch on command
(defn -main [command & args]
  (case command
    "conway"  (apply conway/main args)
    "dice"    (apply dice/main args)  ;; in-file then out-file
    "id3"     (apply id3/main args)
    "spotify" (apply spotify/main args)
    "server"  (mount/start)
    "discord" (discord-run!)
    (log/error "command must be one of [conway dice id3 spotify server]")))
