(ns hub.inventory
  (:require [hub.util.data-file :as data-file]))

(def albums-csv "inventory/albums.csv")

(defn albums
  "Vector of all albums in my inventory."
  []
  (data-file/load-csv albums-csv))

(defn album->vec
  [{:keys [artist release ownership]}]
  [artist release ownership])

(defn add-albums
  "Write a sequence of albums to a CSV."
  [albums]
  (data-file/append-csv albums-csv (map album->vec albums)))

(defn add-album [album]
  (add-albums [album]))

;; TODO: I dunno if I even like this. In the REPL it's nice, probably less
;; so on the front-end.
(defn sieve
  "Filter a seq of maps by key `k`'s value `v`, remove that key that from the map.
  Only for exact-match filtering, since you still know the value in that key."
  [m k v]
  (->> m
       (filter #(= v (k %)))
       (map #(dissoc % k))))

(defn ownership
  "Lazy sequence of all `albums` with matching Ownership `value`."
  [albums value]
  {:pre [(some #{"CD" "Vinyl" "Digital"} value)]}
  (sieve albums :ownership value))

(defn by-artist
  "Get all albums by `artist`."
  [albums artist]
  (sieve albums :artist artist))

(defn release
  [albums release-name]
  (sieve albums :release release-name))

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
