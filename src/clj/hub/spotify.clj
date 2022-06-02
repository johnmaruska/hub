(ns hub.spotify
  (:require
   [clojure.tools.logging :as log]
   [environ.core :refer [env]]
   [hub.spotify.me :as my]
   [hub.spotify.artist :as artist]
   [hub.spotify.auth :as auth]
   [hub.spotify.playlist :as playlist]
   [hub.spotify.tracks :as tracks]
   [hub.util :refer [find-by]]
   [hub.util.file :as file]))

;; TODO: move into a configuration
(def playlists-to-sort #{"Discover Weekly" "Release Radar"})
(def artists-file "generated-data/spotify/artists.edn")
(def related-artists-file "generated-data/spotify/related-artists.edn")

;; TODO: play with values to see if we get better results
(defn playlist-priority
  "How to prioritize playlists -- currently by danceability and energy."
  [{:keys [features]}]
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
  (let [all (my/playlists)]
    (letfn [(target-id [playlist]
              (let [target-name (str "HUB - " (:name playlist))]
                (:id (find-by :name target-name all))))
            (generate [source]
              (->> (sort-playlist source)
                   (map (comp :uri :track))
                   (playlist/replace-contents (target-id source))))]
      (->> all
           (filter #(contains? playlists-to-sort (:name %)))
           (run! generate)))))

(defn generate-saved-artists
  "Pull all saved artists from Spotify for currently authorized user and
  persist that to a local file."
  []
  (let [artists (map #(select-keys % [:id :name]) (my/artists))]
    (log/info "Writing artists")
    (file/write-edn artists-file artists)))

(defn read-adjacency-list [file]
  (file/load-edn file))

(defn write-adjacency-list [file contents]
  (log/info "Writing adjacency list")
  (file/write-edn file contents))

(defn new-artists
  "Filter `all-artists` down, removing any that appear in `prev-adj-list`."
  [prev-adj-list all-artists]
  (let [old-artists (into #{} (keys prev-adj-list))
        old-artist? #(contains? old-artists (:name %))]
    (remove old-artist? all-artists)))

(defn generate-related-artist-adjacency-list
  "Requests related artists from Spotify for all artists in `artists-file`.
  `artists-file` must be generated before the adjacency list."
  []
  (let [prev-adj-list (if (file/exists? related-artists-file)
                        (read-adjacency-list related-artists-file)
                        {})]
    ;; TODO: load and stream instead of storing in memory and writing at the end
    (->> (file/load-edn artists-file)
         (new-artists prev-adj-list)
         artist/related-adjacency-list
         (merge prev-adj-list)
         (write-adjacency-list related-artists-file))))

(defn main [& [subcommand & _args]]
  (when (#{"artists" "artists+adjacency" "all"} subcommand)
    (log/info "Generating saved artists")
    (auth/manual-auth (or (env :port) 4000))
    (generate-saved-artists))
  (when (#{"adjacency" "artists+adjacency" "all"} subcommand)
    (log/info "Generating related artists adjacency list")
    (generate-related-artist-adjacency-list))
  (when (#{"playlists" "all"} subcommand)
    (log/info "Generating sorted playlists")
    (auth/manual-auth (or (env :port) 4000))
    (generate-sorted-playlists))
  (when (nil? subcommand)
    (log/error "Expected subcommand to be one of [artists, adjacency, artists+adjacency, playlists, all]")))
