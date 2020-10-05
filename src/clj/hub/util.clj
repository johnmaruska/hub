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
  [reader & {:keys [separator quote]
             :or {separator \, quote \"}}]
  (csv-data->maps (csv/read-csv reader :separator separator)))

(defn load-csv
  "Read `filename` csv into a vector of maps."
  [filename & options]
  (with-open [reader (io/reader (io/resource filename))]
    (vec (apply stream-csv reader options))))

(defn write!
  [filename contents & opts]
  (apply spit (io/resource filename) contents opts))

(defn find-by [k v coll]
  (first (filter #(= v (k %)) coll)))
