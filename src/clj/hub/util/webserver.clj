(ns hub.util.webserver
  (:require
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :refer [run-jetty]]))

(def default-handler
  (ring/create-default-handler
   {:not-found (constantly {:status 404 :body "Not found"})}))

(defn default-router [routes]
  (ring/router routes
               {:data {:coercion   reitit.coercion.malli/coercion
                       :muuntaja   m/instance
                       :middleware [parameters/parameters-middleware
                                    muuntaja/format-negotiate-middleware
                                    muuntaja/format-response-middleware
                                    rrc/coerce-exceptions-middleware
                                    muuntaja/format-request-middleware
                                    rrc/coerce-request-middleware
                                    rrc/coerce-response-middleware]}}))

(def minimal-setup (comp ring/ring-handler default-router))

(defn start! [app port]
  (run-jetty app {:port (Integer. port) :join? false}))

(defn stop! [webserver] (.stop webserver))
