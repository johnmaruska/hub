(ns hub.spotify.me
  (:require
   [hub.spotify.util :refer [api crawl! get!]]))

(defn all-my [entity]
  (crawl! (api "/v1/me/" entity "?limit=50")))

(def playlists (partial all-my "playlists"))
(def tracks    (partial all-my "tracks"))
(def shows     (partial all-my "shows"))

;; This times out the REPL because of how many saved songs I have...
(defn artists []
  (->> (all-my "tracks")  ; (:items (get! (api "/v1/me/tracks?limit=50")))
       (map (comp :artists :track))
       (apply concat)
       distinct))

(defn my-top
  "entity may be `tracks` or `artists`
  time-range may be `long_term`, `medium_term`, `short_term`"
  [entity & {:keys [time-range]}]
  (let [query-str (when time-range
                    (str "?time_range=" time-range))]
    (all-my (str "top/" entity query-str))))

(def top-tracks  (partial my-top "tracks"))
(def top-artists (partial my-top "artists"))
