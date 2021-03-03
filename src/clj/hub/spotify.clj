(ns hub.spotify
  (:require
   [hub.spotify.me :as my]
   [hub.spotify.artist :as artist]
   [hub.spotify.playlist :as playlist]
   [hub.spotify.tracks :as tracks]
   [hub.util :refer [find-by]]
   [hub.util.data-file :as data-file]))

;; TODO: move into a configuration
(def playlists-to-sort #{"Discover Weekly" "Release Radar"})
(def file
  {:artists         "spotify/artists.edn"
   :audio-features  "spotify/audio-features.edn"
   :related-artists "spotify/related-artists.edn"})

;;;; Sort playlists

(defn sort? [playlist]
  (contains? playlists-to-sort (:name playlist)))

(defn sorted-id
  "Get the ID for the sorted-version of `playlist`."
  [all playlist]
  (let [target-name (str "HUB - " (:name playlist))]
    (:id (find-by :name target-name (my/playlists)))))

;; TODO: play with values to see if we get better results
(defn playlist-priority [{:keys [features]}]
  (* -1
     (:danceability features)
     (:energy features)))

(defn sort-playlist [playlist]
  (let [tracks   (playlist/get-tracks playlist)
        features (tracks/audio-features tracks)
        enriched (tracks/enrich tracks features)]
    (sort-by playlist-priority (vals enriched))))

;; TODO: this should be a weekly cron job set to happen between Spotify
;; generating playlists and waking up that morning.
(defn generate-sorted-playlists
  "Create sorted version of specified playlists into new playlists named
  format `HUB - <name>`."
  []
  (let [generate! (fn [source]
                    (->> (sort-playlist source)
                         (map (comp :uri :track))
                         (playlist/replace-contents (sorted-id source))))]
    (run! generate! (filter sort? (my/playlists)))))

(defn generate-saved-artists
  "Pull all saved artists from Spotify for currently authorized user and
  persist that to a local file."
  []
  (let [artists (map #(select-keys % [:id :name]) (my/artists))]
    (data-file/write-edn (:artists file) artists)))

(defn new-artists [prev-adj-list all-artists]
  (let [old-artists (into #{} (keys prev-adj-list))
        old-artist? #(contains? old-artists (:name %))]
    (remove old-artist? all-artists)))

(defn generate-related-artist-adjacency-list
  "Requests related artists from Spotify for all artists in `artists-file`.
  `artists-file` must be generated before the adjacency list."
  []
  (let [prev-adj-list (data-file/load-edn (:related-artists file))]
    (->> (data-file/load-edn (:artists file))
         (new-artists prev-adj-list)
         artist/related-adjacency-list
         (merge prev-adj-list)
         (data-file/write-edn (:related-artists file)))))


(defn download-audio-features []
  (->> (my/tracks)
       tracks/audio-features
       (data-file/write-edn (:audio-features file))))
