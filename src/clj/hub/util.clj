(ns hub.util
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]))

(defn find-by [k v coll]
  (first (filter #(= v (k %)) coll)))

(defn parse-json [s]
  (json/read-str s :key-fn keyword))

(defn remove-prefix [s prefix]
  (if (string/starts-with? s prefix)
    (string/triml (string/replace-first s prefix ""))
    s))
