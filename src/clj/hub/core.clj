(ns hub.core
  (:require
   [hub.cli.id3-fix :as id3]
   [hub.conway :as conway]
   [hub.dice :as dice]
   [hub.spotify :as spotify]
   [hub.discljord.core :as discord]
   [hub.server :as server]
   [clojure.tools.logging :as log])
  (:gen-class))

(def main-fn
  {"conway"    conway/main
   "dice"      dice/main ;; in-file then out-file
   "id3"       id3/main
   "spotify"   spotify/main
   "webserver" server/main
   "discord"   discord/main
   "server"    (fn [& args]
                 (apply server/main args)
                 (apply discord/main args))})

;; TODO: proper monorepo doesn't just switch on command
(defn -main [command & args]
  (if-let [main (main-fn command)]
    (apply main args)
    (log/error "command must be one of " (keys main-fn))))
