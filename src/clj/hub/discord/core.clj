(ns hub.discord.core
  "Commands for running the discord bot."
  (:require [discord.bot :as bot]))

;; extensions are added to a registry with defextension, they don't have to be
;; passed to start in any way.

(defn start! [& args] (bot/start))
