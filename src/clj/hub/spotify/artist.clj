(ns hub.spotify.artist
  (:require [hub.spotify.util :refer [api get!]]
            [clojure.tools.logging :as log]))

(defn related [artist-id]
  (:artists (get! (api "/v1/artists/" artist-id "/related-artists"))))

(defn related-adjacency-list
  [artists]
  (->> artists
       (map (fn [artist]
              (log/info "Requesting artist" (:name artist))
              [(:name artist) (map :name (related (:id artist)))]))
       (into {})))
