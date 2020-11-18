(ns user
  (:require [hub.server :as server]))

(def webserver (atom nil))

(defn stop-jetty! []
  (when @webserver
    (.stop @webserver)
    (reset! webserver nil)))

(defn start-jetty! []
  (stop-jetty!)  ; don't make multiple
  (reset! webserver (server/start!)))
