(ns hub.core
  (:require
   [hub.cli.id3-fix :as id3]
   [hub.conway :as conway]
   [hub.dice :as dice]
   [hub.spotify :as spotify]
   [hub.discljord.core :as discord]
   [hub.server :as server]
   [mount.core :as mount :refer [defstate]]
   [clojure.tools.logging :as log])
  (:gen-class))

;; TODO: proper monorepo doesn't just switch on command
(defn -main [command & args]
  (case command
    "conway"  (apply conway/main args)
    "dice"    (apply dice/main args)  ;; in-file then out-file
    "id3"     (apply id3/main args)
    "spotify" (apply spotify/main args)
    "server"  (apply server/main args)
    "discord" (apply discord/main args)
    (log/error "command must be one of [conway dice id3 spotify server]")))
