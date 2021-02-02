(ns hub.spotify
  (:require [hub.spotify.util :refer [crawl!]]))

(defn url [endpoint]
  (str "https://api.spotify.com" endpoint))

(defn new-releases []
  (crawl! (url "/v1/browse/new-releases")))
