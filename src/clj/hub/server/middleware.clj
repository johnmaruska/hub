(ns hub.server.middleware
  (:require
   [clojure.pprint :refer [pprint]]
   [muuntaja.middleware :refer [wrap-format]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params
                                           keyword-params-request]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.reload :refer [wrap-reload]]))

(defn parse-params [handler]
  (-> handler
      ;; wrap-keyword params above wrap-params
      ;; modifies :params added by wrap-params
      wrap-keyword-params
      wrap-params))

(defn http-format-negotiation [handler]
  (wrap-format handler))

(defn data-routes [handler]
  (-> handler
      parse-params
      http-format-negotiation))

(defn all [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-reload))
