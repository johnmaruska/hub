(ns hub.util
  (:require
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [clojure.data.json :as json]
   [clojure.string :as string])
  (:import
   (java.io PushbackReader)))

(defn find-by [k v coll]
  (first (filter #(= v (k %)) coll)))

(defn parse-json [s]
  (json/read-str s :key-fn keyword))

(defn load-edn [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (edn/read (PushbackReader. reader))))

(defn remove-prefix [s prefix]
  (if (string/starts-with? s prefix)
    (string/triml (string/replace-first s prefix ""))
    s))

(defmacro swallow-exception
  {:style/indent 1}
  [pred & body]
  `(try
     ~@body
     (catch Exception ex#
       (if (not (~pred (ex-data ex#)))
         (throw ex#)
         (log/error ex#)))))
