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

(defn output [{:keys [output] :as recipe}]
  (if (map? output)
    (get output (:name recipe))
    output))

(defn consumption-ratio
  "Return ratios of Consumed ingredients per Generated parts"
  [part-name]
  (let [recipe (parts-recipes part-name)]
    (reduce (fn [coll [k v]]
              (assoc coll k (/ v (output recipe))))
            {}
            (:ingredients recipe))))

(defn update-keys [m f]
  (reduce-kv (fn [coll k v] (assoc coll k (f v))) {} m))

(defn raw-materials [root-part]
  (letfn [(add-mat [mats {:keys [name amount]}]
            (update mats name (fnil + 0) amount))
          (get-parent-parts [part]
            (map (fn [[k v]]
                   {:name   k
                    :amount (get (consumption-ratio part) k)})
                 (:ingredients (parts-recipes part))))]
    (loop [curr-part   {:name root-part :amount 1}
           raw-mats    {}
           ingredients []]
      (cond
        (and (empty? ingredients) (not curr-part))
        (update-keys raw-mats #(int (Math/ceil %)))

        (part? (:name curr-part))
        (let [parents (->> (get-parent-parts (:name curr-part))
                           (map #(update % :amount * (:amount curr-part))))]
          (recur (first parents)
                 raw-mats
                 (concat (rest parents) ingredients)))

        :else
        (recur (first ingredients)
               (add-mat raw-mats curr-part)
               (rest ingredients))))))
