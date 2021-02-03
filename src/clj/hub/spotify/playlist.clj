(ns hub.spotify.playlist
  (:require
   [hub.spotify.util :refer [api crawl! get! request!]]))

(defn my-playlists []
  (crawl! (api "/v1/me/playlists")))

(defn create-playlist [user-id list-name]
  (request! {:method :post
             :url (api (str "/v1/users/" user-id "/playlists"))
             :form-params {:name   list-name
                           :public false}}))

(def playlist-id "3mAjzQ5oFKWVW7fblI7CmC")

(defn get-items [playlist-id]
  (crawl! (api (str "/v1/playlists/" playlist-id "/tracks")
               {:market "US"})))

;; TODO: scope
(defn add-items [playlist-id item-uris]
  (request! {:method       :post
             :url          (api (str "/v1/playlists/" playlist-id "/tracks"))
             :content-type "application/json"
             :form-params  {:uris item-uris}}))

;; TODO: scope
(defn remove-items [playlist-id uris]
  (request! {:method :delete
             :url (api (str "/v1/playlists/" playlist-id "/tracks"))
             :content-type "application/json"
             :form-params {:tracks (map (fn [x] {:uri x}) uris)}}))

;; TODO: scope
(defn wipe-playlist [playlist-id]
  (->> (get-items playlist-id)
       (map (comp :uri :track))
       (remove-items playlist-id)))

(defn get-tracks [playlist]
  (crawl! (-> playlist :tracks :href)))
