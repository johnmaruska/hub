(ns hub.inventory
  (:require [hub.util :as util]))

(def albums-csv "inventory/albums.csv")

(defn albums
  "Vector of all albums in my inventory."
  []
  (util/load-csv albums-csv))

(defn album->vec
  [{:keys [artist release ownership]}]
  [artist release ownership])

(defn add-albums
  "Write a sequence of albums to a CSV."
  [albums]
  ;; TODO: ensure proper quoting happens
  (util/append-csv albums-csv (map album->vec) albums))

(defn add-album [album]
  (add-albums [album]))

(defn sieve
  "Filter a seq of maps by key `k`'s value `v`, and remove that key that from the map.

  Only intended for exact-match filtering, since you still know the value in that key."
  [m k v]
  (->> m
       (filter #(= v (k %)))
       (map #(dissoc % k))))

(defn ownership
  "Lazy sequence of all albums with matching Ownership value, one of `CD`, `Vinyl`, `Digital`."
  [albums value]
  (sieve albums :Ownership value))

(defn by-artist
  "Get all albums by `artist`."
  [albums artist]
  (sieve albums :Artist artist))

(defn release
  [albums release-name]
  (sieve albums :Release release-name))

;;; helper functions for easier readability

(defn vinyl
  "Lazy sequence of albums owned on Vinyl format."
  ([]
   (vinyl (albums)))
  ([albums]
   (ownership albums "Vinyl")))

(defn cds
  "Lazy sequence of albums owned on CD format."
  ([]
   (cds (albums)))
  ([albums]
   (ownership albums "CD")))
