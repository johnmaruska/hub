(ns hub.cron.spotify
  (:require
   [hub.spotify.me :as my]
   [hub.spotify.playlist :as playlist]
   [hub.spotify.tracks :as tracks]
   [hub.util :refer [find-by]]))

;; TODO: move into a configuration
(def playlists-to-sort
  #{"Discover Weekly" "Release Radar"})

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

(defn generate-sorted [source target-id]
  (->> (sort-playlist source)
       (map (comp :uri :track))
       (playlist/replace-contents target-id)))

;; TODO: this should be a weekly cron job set to happen between Spotify
;; generating playlists and waking up that morning.
(defn generate-sorted-playlists []
  (let [all       (my/playlists)
        target-id (fn [playlist]
                    (let [target-name (str "HUB - " (:name playlist))]
                      (find-id all target-name)))]
    (run! #(generate-sorted %1 (target-id %1))
          (filter sort? all))))
