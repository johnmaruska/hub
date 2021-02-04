(ns hub.spotify.user
  (:require
   [hub.spotify.util :refer [api crawl! get!]]))

(defn my-user []
  (get! (api "/v1/me")))

(defn user-playlists [user-id]
  (crawl! (api "/v1/users/" user-id "/playlists")))
