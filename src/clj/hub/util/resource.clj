(ns hub.util.resource
  (:require
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io])
  (:import
   (java.io PushbackReader))
  (:refer-clojure :rename {spit core-spit}))

(defn reader [filename]
  (io/reader (io/resource filename)))

(defn writer [filename & opts]
  (apply io/writer (io/resource filename) opts))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn load-csv
  "Read `filename` csv into a vector of maps."
  [filename]
  (with-open [f (reader filename)]
    (vec (csv-data->maps (csv/read-csv f)))))

(defn write-csv
  [filename rows & opts]
  (with-open [f (apply writer filename opts)]
    (csv/write-csv f rows)))

(defn append-csv
  [filename rows]
  (write-csv filename rows :append true))

(defn load-edn [filename]
  (with-open [f (reader filename)]
    (edn/read (PushbackReader. f))))

(defn spit [filename contents]
  (core-spit (io/resource filename)
             contents))
