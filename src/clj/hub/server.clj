(ns hub.server
  (:require
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]
   [hiccup.page :refer [html5 include-js include-css]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]))

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

(defroutes main-routes
  (GET "/" [] (index-html))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
      (wrap-defaults site-defaults)
      wrap-reload))

(defn start!
  "non(?)-blocking call to start web-server."
  []
  (run-jetty (app) {:port 4000 :join? false}))
