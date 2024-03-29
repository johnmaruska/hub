(ns hub.spotify.me
  (:require [hub.spotify.util :refer [api crawl!]]))

(def all-my
  (memoize
   (fn all-my* [entity]
     (crawl! (api "/v1/me/" entity "?limit=50")
             {:socket-timeout 2000}))))

(def playlists (partial all-my "playlists"))
(def tracks    (partial all-my "tracks"))
(def shows     (partial all-my "shows"))

(defn artists []
  (->> (all-my "tracks")
       (map (comp :artists :track))
       (apply concat)
       distinct))

(def my-top
  "entity may be `tracks` or `artists`
  time-range may be `long_term`, `medium_term`, `short_term`"
  (memoize
   (fn my-top*
     [entity & {:keys [time-range]}]
     (let [query-str (when time-range
                       (str "?time_range=" time-range))]
       (all-my (str "top/" entity query-str))))))

(def top-tracks  (partial my-top "tracks"))
(def top-artists (partial my-top "artists"))
