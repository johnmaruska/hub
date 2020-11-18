(ns hub.server.inventory
  (:require
   [compojure.core :refer [context defroutes GET POST]]
   [hub.inventory :as inventory]))

;; TODO: what happens with comma-separated route vars? `artist=Abba,Mastodon`
;; answer: it doesn't work, treats it as single name.
;; eventually want to do this

(defn get-albums
  "Handler to GET albums. Assumes 0-or-1 values for each query-param"
  [{:keys [params] :as req}]
  (let [{:keys [artist release ownership]} params
        result (cond-> (inventory/albums)
                 ownership (inventory/ownership ownership)
                 artist    (inventory/by-artist artist)
                 release   (inventory/release release))]
    {:status 200
     :body   {:result result}}))

(defn post-album
  [{:keys [body-params] :as req}]
  ;; TODO: spec validate body-params
  (inventory/add-album body-params)
  {:status 201})

(defroutes albums-routes
  (context "/albums" []
    (GET "/" req
      (get-albums req))
    (POST "/" req
      (post-album req))))

(defroutes routes
  (context "/inventory"[]
    albums-routes))
