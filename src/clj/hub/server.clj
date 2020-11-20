(ns hub.server
  (:require
   [compojure.core :as compojure :refer [GET]]
   [compojure.route :as route]
   [hiccup.page :refer [html5 include-js include-css]]
   [hub.server.inventory :as inventory]
   [hub.server.middleware :as middleware]
   [reitit.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]))

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

(def data-routes
  (-> (compojure/routes
       inventory/routes)
      (compojure/wrap-routes middleware/data-routes)))

(def app-compojure
  (compojure/routes
   (GET "/" req (index-html req))
   data-routes
   (route/resources "/")
   (route/not-found "Page not found")))

(def app-reitit
  (ring/ring-handler
   (ring/router
    [["/" {:handler index-html}]])
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not found"})})))

(defn start!
  "non(?)-blocking call to start web-server."
  []
  (run-jetty #'app-reitit {:port 4000 :join? false}))
