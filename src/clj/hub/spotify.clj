(ns hub.spotify
  (:require [hub.spotify.util :refer [crawl! get!]]))

(defn url [endpoint]
  (str "https://api.spotify.com" endpoint))

(defn new-releases []
  (crawl! (url "/v1/browse/new-releases")))

(defn my-playlists []
  (get! (url "/v1/me/playlists")))
