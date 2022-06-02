(ns hub.util.file
  (:require
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [hub.util :refer [parse-json]])
  (:import
   (java.io PushbackReader)))

(defn exists? [file]
  (.exists (io/file file)))

(defn csv-data->maps
  "Convert `csv-data` to maps.

  Assumes first entry is headers."
  [csv-data]
  (let [headers (map keyword (first csv-data))]
    (map zipmap
         (repeat headers)
         (rest csv-data))))

(defn load-csv
  "Read `filename` csv into a vector of maps."
  [filename]
  (with-open [f (io/reader filename)]
    (vec (csv-data->maps (csv/read-csv f)))))

(defn write-csv
  "Write `rows` to `filename` in csv format. Pass `opts` to `io/writer`."
  [filename rows & opts]
  (with-open [f (apply io/writer filename opts)]
    (csv/write-csv f rows)))

(defn append-csv
  [filename rows]
  (write-csv filename rows :append true))

(defn load-json [file]
  (parse-json (io/reader file)))

(defn load-edn [file]
  (with-open [f (io/reader file)]
    (edn/read (PushbackReader. f))))

(defn write-edn [filename contents]
  (pprint contents (io/writer filename)))
