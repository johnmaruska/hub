(ns hub.server
  (:require
   [compojure.core :as compojure :refer [GET]]
   [compojure.route :as route]
   [hiccup.page :refer [html5 include-js include-css]]
   [hub.server.inventory :as inventory]
   [hub.server.middleware :as middleware]
   [ring.adapter.jetty :refer [run-jetty]]))

(defn index-html []
  (html5
   [:head
    [:title "Maruska Hub"]
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (include-css "/css/style.css")]
   [:body
    [:h2 "Clojure Function!"]
    [:div {:id "app"}]
    (include-js "/cljs-out/dev-main.js")]))

(def data-routes
  (-> (compojure/routes
       inventory/routes)
      (compojure/wrap-routes middleware/data-routes)))

(def app
  (compojure/routes
   (GET "/" [] (index-html))
   data-routes
   (route/resources "/")
   (route/not-found "Page not found")))

(defn start!
  "non(?)-blocking call to start web-server."
  []
  (run-jetty #'app {:port 4000 :join? false}))
