(ns hub.core
  (:require
   [hub.conway :as conway]
   [hub.conway.seed :as conway.seed]
   [hub.discljord.core :as discord]
   [hub.server :as server])
  (:gen-class))

(defn run-discord
  "Initialize and run Discord bot. Blocking."
  []
  (let [bot (discord/start!)]
    (discord/spin-forever! bot)))

(defn run-conway []
  (-> (conway.seed/all-dead 50 50)
      (conway.seed/overlay conway.seed/glider 5 5)
      conway/console-print))

(def webserver (atom nil))
(defn webserver-start! []   (reset! webserver (server/start!)))
(defn webserver-stop! []    (when @webserver (.stop @webserver)))
(defn webserver-restart! [] (do (webserver-stop!) (webserver-start!)))

(defn -main [& args]
  (println "hub core")
  (doseq [arg *command-line-args*]
    (println "arg" arg))
  )
