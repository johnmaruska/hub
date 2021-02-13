(ns hub.spotify.artist
  (:require [hub.spotify.util :refer [api crawl! get!]]))

(defn related [artist-id]
  (:artists (get! (api "/v1/artists/" artist-id "/related-artists"))))

(defn related-adjacency-list
  [artists]
  (->> artists
       (map (fn [artist]
              (println "Requesting artist" (:name artist))
              [(:name artist) (map :name (related (:id artist)))]))
       (into {})))
