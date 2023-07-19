(ns hub.util.file
  (:require
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]
   [hub.util :refer [parse-json]])
  (:import
   (java.io File FileOutputStream PushbackReader)))

(defn exists? [file]
  (.exists (io/file file)))

(defn csv-data->maps
  "Convert `csv-data` to maps.

  Assumes first entry is headers."
  [csv-data & [key-fn]]
  (let [headers (map (or key-fn keyword)
                     (first csv-data))]
    (map zipmap
         (repeat headers)
         (rest csv-data))))

(defn read-csv [reader & opts]
  (csv-data->maps (apply csv/read-csv reader opts)))
(defn load-csv [filename & opts]
  (with-open [f (io/reader filename)]
    (vec (apply read-csv f opts))))

(defn read-tsv [reader]
  (read-csv reader :separator \tab))
(defn load-tsv [filename]
  (load-csv filename :separator \tab))

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

(defn absolute-path [^File file]
  (.. file toPath toAbsolutePath))

(defn copy [stream local-file]
  (.write (FileOutputStream. local-file) (.readAllBytes stream)))

(defn gunzip
  "Unzip a .gz file to uncompressed version, e.g. foo.tsv.gz -> foo.tsv"
  [gz-file]
  (let [outfile (io/file (string/replace (.toString (absolute-path gz-file))
                                         #".gz$" ""))]
    (with-open [input-stream (java.util.zip.GZIPInputStream. (io/input-stream gz-file))]
      (copy input-stream outfile))
    outfile))

(comment

  (gunzip (io/file "/Users/johnmaruska/Downloads/name.basics.tsv.gz"))
  )
