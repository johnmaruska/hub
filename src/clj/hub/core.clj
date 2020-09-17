(ns hub.core
  (:require [hub.discord.core :as discord])
  (:gen-class))

(defn destroy! [& args])

(defn init! [& args]
  (discord/start!))

(defn -main [& args]
  (init!))
