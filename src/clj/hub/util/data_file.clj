(ns hub.util.data-file
  (:require
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]])
  (:import
   (java.io PushbackReader)))

(defn reader [filename]
  (io/reader (str "data/" filename)))

(defn writer [filename & opts]
  (apply io/writer (str "data/" filename) opts))

(defn csv-data->maps
  "Convert `csv-data` to maps.

  Assumes first entry is headers."
  [csv-data]
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
  "Write `rows` to `filename` in csv format. Pass `opts` to `io/writer`."
  [filename rows & opts]
  (with-open [f (apply writer filename opts)]
    (csv/write-csv f rows)))

(defn append-csv
  [filename rows]
  (write-csv filename rows :append true))

(defn load-edn [filename]
  (with-open [f (reader filename)]
    (edn/read (PushbackReader. f))))

(defn write-edn [filename contents]
  (pprint contents (writer filename)))
