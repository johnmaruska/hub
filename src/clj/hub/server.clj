(ns hub.server
  (:require
   [hiccup.page :refer [html5 include-js include-css]]
   [hub.server.inventory :as inventory]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
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

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:handler index-html}]
     ["/inventory"
      ["/albums" {:get inventory/get-albums-route}]]]
    {:data {:coercion   reitit.coercion.malli/coercion
            :muuntaja   m/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}})
   (ring/create-default-handler
    {:not-found (constantly {:status 404 :body "Not found"})})))

(defn start!
  "non(?)-blocking call to start web-server."
  []
  (run-jetty #'app {:port 4000 :join? false}))
