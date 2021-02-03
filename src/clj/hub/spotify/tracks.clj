(ns hub.spotify.tracks
  (:require
   [clojure.string :as string]
   [hub.spotify.util :as util]))

(defn group-by-id [f xs]
  (->> xs
       (group-by f)
       (reduce (fn [acc [k v]]
                 (assoc acc k (first v)))
               {})))

(defn audio-features
  [tracks]
  {:pre [(>= 100 (count tracks))]}
  (let [ids (map (comp :id :track) tracks)
        url (str "/v1/audio-features?ids=" (string/join "," ids))]
    (:audio_features (util/get! (util/api url)))))

(defn enrich
  "Request audio features for a collection of tracks."
  [tracks features]
  (reduce (fn [acc [k v]]
            (assoc-in acc [k :features] v))
          (group-by-id (comp :id :track) tracks)
          (group-by-id :id features)))
