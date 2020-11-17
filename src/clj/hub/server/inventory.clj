(ns hub.server.inventory)

;; TODO: what happens with comma-separated route vars? `artist=Abba,Mastodon`

(defn get-albums
  "Handler to GET albums. Assumes 0-or-1 values for each query-param"
  [{:keys [query-params] :as req}]
  (let [{:keys [artist release ownership]}
        result (cond-> (inventory/albums)
                 ownership (inventory/ownership ownership)
                 artist    (inventory/by-artist artist)
                 release   (inventory/release))]
    {:status 200
     :body   {:result result}}))

(defn post-album
  [{:keys [body-params] :as req}]
  ;; TODO: spec validate body-params
  (inventory/add-album body-params)
  {:status 201})

(defroutes albums-routes
  (context "/albums" []
    (GET "/" req) (get-albums req)
    (POST "/" req) (post-album req)))

(defroutes routes
  (context "/inventory"[]
    albums-routes))
