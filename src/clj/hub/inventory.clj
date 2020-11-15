(ns hub.inventory
  (:require [hub.util :as util]))

(defn albums
  "Vector of all albums in my inventory."
  []
  (util/load-csv "inventory/albums.csv"))

(defn ownership
  "Lazy sequence of all albums with matching Ownership value, one of `CD`, `Vinyl`, `Digital`."
  [albums value]
  (->> albums
       (filter #(= value (:Ownership %)))
       (map #(dissoc % :Ownership))))

(defn vinyl
  "Lazy sequence of all albums owned on Vinyl format."
  ([]
   (vinyl (albums)))
  ([albums]
   (ownership albums "Vinyl")))

(defn cds
  "Lazy sequence of all albums owned on CD format."
  ([]
   (cds (albums)))
  ([albums]
   (ownership albums "CD")))


(defn by-artist
  "Get all albums by artist name `n`."
  ([artist]
   (by-artist (albums) artist))
  ([albums artist]
   (->> albums
        (filter #(= artist (:Artist %)))
        (map #(dissoc % :Artist)))))

(by-artist (vinyl (albums)) "Black Sabbath")
