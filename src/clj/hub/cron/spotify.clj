(ns hub.cron.spotify
  (:require
   [hub.spotify.me :as my]
   [hub.spotify.artist :as artist]
   [hub.spotify.playlist :as playlist]
   [hub.spotify.tracks :as tracks]
   [hub.util :refer [find-by]]
   [hub.util.resource :as resource]
   [clojure.java.io :as io]))

;; TODO: move into a configuration
(def playlists-to-sort
  #{"Discover Weekly" "Release Radar"})

(def artists-file "spotify/artists.edn")
(def related-artists-file "spotify/related-artists.edn")

;; TODO: play with values to see if we get better results
(defn playlist-priority [{:keys [features]}]
  (* -1
     (:danceability features)
     (:energy features)))

(defn sort? [playlist]
  (contains? playlists-to-sort (:name playlist)))

(defn sort-playlist [playlist]
  (let [tracks   (playlist/get-tracks playlist)
        features (tracks/audio-features tracks)
        enriched (tracks/enrich tracks features)]
    (sort-by playlist-priority (vals enriched))))

(defn find-id [playlists target-name]
  (:id (find-by :name target-name playlists)))

;; TODO: this should be a weekly cron job set to happen between Spotify
;; generating playlists and waking up that morning.
(defn generate-sorted-playlists []
  (let [all       (my/playlists)
        target-id (fn [playlist]
                    (let [target-name (str "HUB - " (:name playlist))]
                      (find-id all target-name)))
        generate  (fn [source]
                    (->> (sort-playlist source)
                         (map (comp :uri :track))
                         (playlist/replace-contents (target-id source))))]
    (run! generate (filter sort? all))))


;;; WARNING: these take a while to run and can't be executed in the REPL.
;;; store intermediate results in a file
(defn generate-saved-artists []
  (resource/spit artists-file
                 (my/artists)))

(defn generate-related-artist-adjacency-list []
  (->> (resource/load-edn artists-file)
       (artist/related-adjacency-list)
       (resource/spit related-artists-file)))
