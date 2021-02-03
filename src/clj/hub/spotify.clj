(ns hub.spotify
  (:require
   [hub.spotify.tracks :as tracks]
   [hub.spotify.util :refer [crawl! get! api]]))

(defn new-releases []
  (crawl! (api "/v1/browse/new-releases")))

(defn my-playlists []
  (crawl! (api "/v1/me/playlists")))

(def playlists-to-sort
  #{"Discover Weekly" "Release Radar"})

(defn sort? [playlist]
  (contains? playlists-to-sort (:name playlist)))

(defn playlist-priority [{:keys [features]}]
  (* (:danceability features)
     (:energy features)))

(defn sort-playlist [playlist]
  (let [tracks   (get-tracks playlist)
        features (tracks/audio-features tracks)
        enriched (tracks/enrich tracks features)]
    (sort-by playlist-priority (vals enriched))))
