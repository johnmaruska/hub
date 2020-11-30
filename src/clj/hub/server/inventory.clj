(ns hub.server.inventory
  (:require
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
    {:status  200
     :headers {"Access-Control-Allow-Origin"  "*"
               "Access-Control-Allow-Headers" "Content-Type"}
     :body    {:results results}}))

(defn post-album
  [{{:keys [body]} :parameters}]
  (inventory/add-album body)
  {:status 201})

(def routes
  ["/inventory"
   ["/albums" {:get  {:handler    get-albums
                      :responses  {200 {:body (results (mu/optional-keys spec/album))}}
                      :parameters {:query (mu/optional-keys spec/album)}}
               :post {:handler    post-album
                      :responses  {201 nil}
                      :parameters {:body spec/album}}}]])
