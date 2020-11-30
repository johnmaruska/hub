(ns user
  (:require [hub.server :as server]))

(def webserver (atom nil))

(defn stop-jetty! []
  (when @webserver
    (println "Stopping Jetty server" @webserver)
    (.stop @webserver)
    (reset! webserver nil)))

(defn start-jetty! []
  (when (nil? @webserver)
    (reset! webserver (server/start!))))

(defn restart-jetty! []
  (do (stop-jetty!)
      (start-jetty!)))
