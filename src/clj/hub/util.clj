(ns hub.util
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn stream-csv
  [reader]
  (csv-data->maps (csv/read-csv reader)))

(defn load-csv
  "Read `filename` csv into a vector of maps."
  [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (vec (stream-csv reader))))

(defn write-csv
  [filename rows & opts]
  (with-open [writer (apply io/writer (io/resource filename) opts)]
    (csv/write-csv writer rows)))

(defn append-csv
  [filename rows]
  (write-csv filename rows :append true))

(defn find-by [k v coll]
  (first (filter #(= v (k %)) coll)))
