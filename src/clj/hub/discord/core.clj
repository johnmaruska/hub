(ns hub.discord.core
  "Commands for running the discord bot.

  Individual commands/interactions get their own namespaces."
  (:require [discord.bot :as bot]))


(defn start!
  [& args]
  (bot/start))
