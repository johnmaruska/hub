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

(defn configure-logging []
  ;; disable noisy verbose logger for claudio.id3
  (.setLevel (java.util.logging.Logger/getLogger "org.jaudiotagger")
             java.util.logging.Level/OFF))

(defstate discord-bot
  :start (discord/start!)
  ;; TODO: thread management with event pump
  :stop  (discord/stop! discord-bot))

(defstate webserver
  :start (server/start!)
  :stop  (.stop webserver))

(defmacro spin-forever [& body]
  `(loop [] ~@body (recur)))

(def manual-kill? (comp :manual-kill? ex-data))

(defmacro spin-until-manual-kill [& body]
  `(swallow-exception?
    manual-kill?
    (spin-forever
     (swallow-exception (comp not manual-kill?)
       ~@body))))

(defn discord-run! []
  (spin-until-manual-kill
   (discord/handle-event! discord-bot)))

;; TODO: proper monorepo doesn't just switch on command
(defn -main [command & args]
  (configure-logging)
  (case command
    "conway"  (apply conway/main args)
    "dice"    (apply dice/main args)  ;; in-file then out-file
    "id3"     (id3/apply-fix!)
    "spotify" (do
                (spotify/generate-saved-artists)
                (spotify/generate-related-artist-adjacency-list)
                (spotify/generate-sorted-playlists))
    "server"  (do
                (mount/start)
                (discord-run!))
    (log/error "command must be one of [conway dice id3 spotify server]")))
