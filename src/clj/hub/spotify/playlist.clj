(ns hub.spotify.playlist
  (:require
   [hub.spotify.util :refer [api crawl! get! request!]]))

(defn my-playlists []
  (crawl! (api "/v1/me/playlists")))

(defn create-playlist [user-id list-name]
  (request! {:method :post
             :url (api "/v1/users/" user-id "/playlists")
             :form-params {:name   list-name
                           :public false}}))

(defn get-items [playlist-id]
  (crawl! (api "/v1/playlists/" playlist-id "/tracks?market=US")))

(defn add-items [playlist-id item-uris]
  (request! {:method       :post
             :url          (api (str "/v1/playlists/" playlist-id "/tracks"))
             :content-type "application/json"
             :form-params  {:uris item-uris}}))

(defn remove-items [playlist-id uris]
  (request! {:method :delete
             :url (api (str "/v1/playlists/" playlist-id "/tracks"))
             :content-type "application/json"
             :form-params {:tracks (map (fn [x] {:uri x}) uris)}}))

(defn uris [item]
  (if-let [linked-from (-> item :track :linked_from :uri)]
    [(-> item :track :uri) linked-from]
    [(-> item :track :uri)]))

(defn wipe-playlist [playlist-id]
  (->> (get-items playlist-id)
       (map uris)
       flatten
       (remove-items playlist-id)))

(defn replace-contents [playlist-id item-uris]
  (wipe-playlist playlist-id)
  (add-items playlist-id item-uris))

(defn get-tracks [playlist]
  (crawl! (-> playlist :tracks :href)))