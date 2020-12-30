(ns hub.core
  (:require
   [hub.conway :as conway]
   [hub.conway.seed :as conway.seed])
  (:gen-class))

(defn -main [& args]
  (println "hub core")
  (doseq [arg *command-line-args*]
    (println "arg" arg))
  (-> (conway.seed/all-dead 50 50)
      (conway.seed/overlay conway.seed/glider 5 5)
      conway/console-print))
