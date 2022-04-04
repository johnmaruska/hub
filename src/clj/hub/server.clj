(ns hub.server
  (:require
   [environ.core :refer [env]]
   [hiccup.page :refer [html5 include-js include-css]]
   [hub.util.webserver :refer [default-app-setup start! stop!]]
   [hub.server.inventory :as inventory]
   [mount.core :as mount :refer [defstate]]))

(defn index-html [_request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (html5
             [:head
              [:title "Maruska Hub"]
              [:meta {:charset "UTF-8"}]
              [:meta {:name    "viewport"
                      :content "width=device-width, initial-scale=1"}]
              (include-css "/css/style.css")]
             [:body
              [:h2 "Clojure Function!"]
              [:div {:id "app"}]
              (include-js "/cljs-out/dev-main.js")])})

(def app
  (default-app-setup
   [["/" {:handler #'index-html}]
    inventory/routes]))

(defn main [& _args]
  (defstate webserver
    :start (start! #'app (or (env :port) 4000))
    :stop  (stop! webserver))
  (mount/start))
