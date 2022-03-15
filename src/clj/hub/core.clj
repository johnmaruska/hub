(ns hub.core
  (:require
   [hub.cli.id3-fix :as id3]
   [hub.conway :as conway]
   [hub.dice :as dice]
   [hub.spotify :as spotify]
   [hub.discljord.core :as discord]
   [hub.server :as server]
   [hub.util :refer [swallow-exception]]
   [mount.core :as mount :refer [defstate]])
  (:gen-class))

(defstate discord-bot
  :start (discord/start!)
  ;; TODO: thread management with event pump
  :stop  (discord/stop! discord-bot))

(defstate webserver
  :start (server/start!)
  :stop  (.stop webserver))

(defn running? [th] (and th @(.closed th)))
(defn stop! [th] (when (running? th) (.close! th)))

(defmacro spin-forever!
  "Repeat execution of a block of code indefinitely.
  Breaks only on uncaught exception.
  Mostly this is used for the Discord bot event-pump in the REPL since
  it hasn't been deployed yet, and will be used with some form of async
  when deployed."
  [& body]
  `(loop [] ~@body (recur)))

(defn discord-run! []
  (let [manual-kill? (comp :manual-kill? ex-data)]
    ;; end run but dont crash application
    (swallow-exception manual-kill?
      (spin-forever!
       ;; most exceptions should just log and continue on
       (swallow-exception (comp not manual-kill?)
         (discord/handle-event! discord-bot))))))

;; TODO: proper monorepo doesn't just switch on command
(defn -main [command & _args]
  (case command
    "conway"  (conway/sketch-animate)
    "dice"    (dice/task (System/getenv "DICE_INFILE") (System/getenv "DICE_OUTFILE"))
    "id3"     (id3/apply-fix!)
    "spotify" (do
                (spotify/generate-saved-artists)
                (spotify/generate-related-artist-adjacency-list)
                (spotify/generate-sorted-playlists))
    "server"  (do
                (mount/start)
                (discord-run!))))
