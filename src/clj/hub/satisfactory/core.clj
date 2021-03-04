(ns hub.satisfactory.core
  (:require
   [hub.util.data-file :as data-file]
   [clojure.java.io :as io]))

(defn parse-map-column
  "columns in the csv which correspond to a map are of form `key=digit`.
  If no match, assume form `digit`."
  [column]
  (if-let [matches (re-seq #"([a-z-]+)=(\d+)" column)]
    (reduce (fn [m [_ k v]]
              (assoc m k (Integer/parseInt v)))
            {}
            matches)
    (Integer/parseInt column)))

(defn parse-recipe [recipe]
  (-> recipe
      (update :time #(Integer/parseInt %))
      (update :output parse-map-column)
      (update :ingredients parse-map-column)))

(def parts-recipes
  (->> (io/resource "satisfactory/ingredients.csv")
       data-file/load-csv
       (map parse-recipe)
       (reduce (fn [coll m] (assoc coll (:name m) m)) {})))

;;; these only work for parts, not raw ingredients

(defn part? [part-name]
  (boolean (get-in parts-recipes [part-name :ingredients])))

(defn consumption-ratio
  "Return ratios of Consumed ingredients per Generated parts"
  [part-name]
  (let [recipe (parts-recipes part-name)]
    (reduce (fn [coll [k v]]
              (assoc coll k (/ v (:output recipe))))
            {}
            (:ingredients recipe))))

(defn raw-materials [part]
  (reduce (fn [coll [ingredient amount]]
            (if (part? ingredient)
              (->> (raw-materials ingredient)
                   (map (fn [[k v]]
                          [k (* amount v)]))
                   (into {})
                   (merge-with + coll))
              (assoc coll ingredient amount)))
          {}
          (:ingredients (parts-recipes part))))

(raw-materials "rotor")
