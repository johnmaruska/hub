(ns user
  (:require
   [clojure.core.async :as async]
   [clojure.java.io :as io]
   [hub.discljord.core :as discord]
   [mount.core :as mount]))

(defn mount-restart []
  (mount/stop)
  (mount/start))

;;; Discord Bot

(defonce discord-bot (atom nil))
(defonce discord-channel (atom nil))

(defn discord-start! []
  (when (not (or @discord-bot @discord-channel))
    (reset! discord-bot (discord/connect!))))

(defn discord-stop-spin! []
  (when @discord-channel
    (async/close! @discord-channel)
    (reset! discord-channel nil)))

(defn discord-stop-bot! []
  (when @discord-bot
    (discord/disconnect! @discord-bot)
    (reset! discord-bot nil)))

(def discord-stop! (comp discord-stop-bot! discord-stop-spin!))
(def discord-restart! (comp discord-start! discord-stop!))

(defn discord-spin! []
  (when (and @discord-bot (not @discord-channel))
    (reset! discord-channel (discord/run-bot! @discord-bot))))

(defn discord-restart-spin! []
  (do (discord-spin!) (discord-stop-spin!)))
