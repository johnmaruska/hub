(ns hub.spotify.tracks
  (:require
   [clojure.string :as string]
   [hub.spotify.util :refer [api get!]]))

(defn group-by-id [f xs]
  (->> xs
       (group-by f)
       (reduce (fn [acc [k v]]
                 (assoc acc k (first v)))
               {})))

(defn audio-features*
  [tracks]
  {:pre [(>= 100 (count tracks))]}
  (let [ids (->> tracks
                 (map (comp :id :track))
                 (string/join ","))]
    (:audio_features (get! (api "/v1/audio-features?ids=" ids)))))

(defn partition*
  "`partition` with padding so remainders aren't dropped."
  [n xs]
  (partition n (count xs) [] xs))

(defn audio-features
  "Request audio features for all required tracks.
  Will chunk requests that are too large and recombine them."
  [tracks]
  (->> tracks
       (partition* 100)
       (map audio-features*)  ; TODO: parallelize
       (apply concat)))

(defn enrich
  "Request audio features for a collection of tracks."
  [tracks features]
  (reduce (fn [acc [k v]]
            (assoc-in acc [k :features] v))
          (group-by-id (comp :id :track) tracks)
          (group-by-id :id features)))
