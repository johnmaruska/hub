(ns hub.server
  (:require
   [environ.core :refer [env]]
   [hub.util.webserver :as webserver]
   [hub.server.inventory :as inventory]
   [mount.core :as mount :refer [defstate]]
   [reitit.ring :as ring]))

(def app
  (ring/ring-handler
   (webserver/default-router
    inventory/routes)
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    webserver/default-handler)))

(defn main [& _args]
  (defstate webserver
    :start (webserver/start! #'app (or (env :port) 4000))
    :stop  (webserver/stop! webserver))
  (mount/start))
