(ns hub.server
  (:require
   [environ.core :refer [env]]
   [hub.util.webserver :refer [default-app-setup start! stop!]]
   [hub.server.inventory :as inventory]
   [mount.core :as mount :refer [defstate]]
   [reitit.ring :as ring]))

(def app
  (default-app-setup
   [inventory/routes
    ["/*" (ring/create-resource-handler)]]
   {:conflicts nil}))

(defn main [& _args]
  (defstate webserver
    :start (start! #'app (or (env :port) 4000))
    :stop  (stop! webserver))
  (mount/start))
