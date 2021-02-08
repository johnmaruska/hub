(ns hub.core
  (:require
   [clojure.core.async :refer [thread]]
   [hub.conway :as conway]
   [hub.conway.seed :as conway.seed]
   [hub.discljord.core :as discord]
   [hub.server :as server]
   [mount.core :as mount :refer [defstate]]
   [clojure.tools.logging :as log])
  (:gen-class))

(defstate discord-bot
  :start (discord/start!)
  ;; TODO: thread management with event pump
  :stop  (discord/stop!))

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

(defmacro swallow-exception
  {:style/indent 1}
  [pred & body]
  `(try
     ~@body
     (catch Exception ex#
       (if (not (~pred (ex-data ex#)))
         (throw ex#)
         (log/error ex#)))))

(defn discord-run! []
  (swallow-exception :manual-kill?
    (spin-forever!
     (swallow-exception (comp not :manual-kill?)
       (discord/handle-event! discord-bot)))))

(defn run-conway []
  (-> (conway.seed/all-dead 50 50)
      (conway.seed/overlay conway.seed/glider 5 5)
      conway/console-print))

(defn -main [& args]
  (mount/start)
  (discord-run!))
