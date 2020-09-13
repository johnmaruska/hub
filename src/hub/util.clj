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

(defn load-csv
  "Read `filename` csv into a vector of maps."
  [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (-> (csv/read-csv reader)
        csv-data->maps
        vec)))

(defn write!
  [filename contents & opts]
  (apply spit (io/resource filename) contents opts))
