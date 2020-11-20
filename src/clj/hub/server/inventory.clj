(ns hub.server.inventory
  (:require
   [compojure.core :refer [context defroutes GET POST]]
   [hub.inventory :as inventory]
   [hub.inventory.spec :as spec]
   [malli.core :as m]
   [malli.util :as mu]))

;; TODO: what happens with comma-separated route vars? `artist=Abba,Mastodon`
;; answer: it doesn't work, treats it as single name.
;; eventually want to do this

;; TODO: when entities accessible individually, RESTy link in responses

(defn results [entity]
  [:map [:results [:vector entity]]])

(defn get-albums
  "Handler to GET albums. Assumes 0-or-1 values for each query-param"
  [{{{:keys [artist release ownership]} :query} :parameters}]
  (let [results (cond-> (inventory/albums)
                  ownership (inventory/ownership ownership)
                  artist    (inventory/by-artist artist)
                  release   (inventory/release release))]
    {:status 200
     :body   {:results results}}))

(def get-albums-route
  {:handler    get-albums
   :responses  {200 {:body (results (mu/optional-keys spec/album))}}
   :parameters {:query (mu/optional-keys spec/album)}})

(defn post-album
  [{:keys [body-params] :as req}]
  (if (m/validate spec/album body-params)
    (do
      (inventory/add-album body-params)
      {:status 201})
    {:status 400 :body "Bad Request"}))
