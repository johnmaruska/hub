(ns hub.sketchbook.satisfactory
  "Mostly just trying to see what number of buildings are required to build
  specific end-game materials."
  (:require
   [clojure.java.io :as io]
   [hub.util :as util :refer [update-keys]]
   [hub.util.file :as file]))

(def ingredients-csv
  (io/resource "data/satisfactory/ingredients.csv"))

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

(defn coerce-recipe [recipe]
  (-> recipe
      (update :time #(Integer/parseInt %))
      (update :output parse-map-column)
      (update :ingredients parse-map-column)))

(def parts-recipes
  (memoize
   #(->> (file/load-csv ingredients-csv)
         (map coerce-recipe)
         (reduce (fn [coll m] (assoc coll (:name m) m)) {}))))

;;; these only work for parts, not raw ingredients

(defn part? [part-name]
  (boolean (get-in (parts-recipes) [part-name :ingredients])))

(defn output [{:keys [output] :as recipe}]
  (if (map? output)
    (get output (:name recipe))
    output))

(defn consumption-ratio
  "Return ratios of Consumed ingredients per Generated parts"
  [part-name]
  (let [recipe ((parts-recipes) part-name)]
    (reduce (fn [coll [k v]]
              (assoc coll k (/ v (output recipe))))
            {}
            (:ingredients recipe))))

(defn add-part [m part]
  (update m (:name part) (fnil + 0) (:amount part)))

(defn get-parent-parts [part]
  (map (fn [[k _]]
         {:name   k
          :amount (get (consumption-ratio part) k)})
       (get-in (parts-recipes) [part :ingredients])))

(defn required-materials [part]
  (->> (get-parent-parts (:name part))
       (map #(update % :amount * (:amount part)))))

(defn raw-materials
  "Calculate total raw materials needed to craft one unit of a part."
  [root-part]
  (->>
   (loop [curr-part      {:name   root-part
                          :amount (get-in (parts-recipes) [root-part :output])}
          all-parts-required {}
          ingredients    []]
     (cond
       (and (empty? ingredients) (not curr-part))
       ;; no more ingredients to check, return seen raw materials
       all-parts-required

       (part? (:name curr-part))
       (let [new-parts-required (required-materials (curr-part))]
         ;; track parent ingredients, move on to next ingredient
         (recur (first new-parts-required)
                all-parts-required
                (concat (rest new-parts-required) ingredients)))

       :else
       ;; track raw material, move on to next ingredient
       (recur (first ingredients)
              (add-part all-parts-required curr-part)
              (rest ingredients))))
   (update-keys #(int (Math/ceil %)))))
