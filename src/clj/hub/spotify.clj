(ns hub.spotify
  (:require
   [hub.spotify.playlist :as playlist]
   [hub.spotify.tracks :as tracks]
   [hub.spotify.util :refer [crawl! get! api]]
   [hub.util :refer [find-by]]))

(defn new-releases []
  (crawl! (api "/v1/browse/new-releases")))
