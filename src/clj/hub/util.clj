(ns hub.util
  (:require
   [clojure.tools.logging :as log]
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

(defn insert-at [xs idx el]
  (let [[lhs rhs] (split-at idx xs)]
    (concat lhs [el] rhs)))

(defmacro swallow-exception
  {:style/indent 1}
  [pred & body]
  `(try
     ~@body
     (catch Exception ex#
       (if (not (~pred (ex-data ex#)))
         (throw ex#)
         (log/error ex#)))))
