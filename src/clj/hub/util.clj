(ns hub.util
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.json :as json]
   [clojure.string :as string])
  (:import
   (java.util UUID)))

(defn uuid []
  (UUID/randomUUID))

(defn find-by [k v coll]
  (first (filter #(= v (k %)) coll)))

(defn parse-json [s]
  (if (instance? java.io.Reader s)
    (json/read s :key-fn keyword)
    (json/read-str s :key-fn keyword)))

(defn remove-prefix [s prefix]
  (if (string/starts-with? s prefix)
    (string/triml (string/replace-first s prefix ""))
    s))

(defn update-vals
  "Apply `f` to each value in `m`."
  [f m]
  (reduce (fn [acc [k xs]]
            (assoc acc k (f xs)))
          {} m))

(defmacro swallow-exception
  {:style/indent 1}
  [pred & body]
  `(try
     ~@body
     (catch Exception ex#
       (if (~pred ex#)
         (log/error ex#)
         (throw ex#)))))
