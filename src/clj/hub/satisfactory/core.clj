(ns hub.satisfactory.core
  (:require [hub.util :as util]))

(def parts-recipes
  (util/load-edn "satisfactory/ingredients.edn"))

;;; these only work for parts, not raw ingredients

(defn part? [recipe]
  (boolean (get-in parts-recipes [recipe :ingredients])))

(defn consumption-rate
  [recipe ingredient]
  (/ (get-in parts-recipes [recipe :ingredients ingredient])
     (get-in parts-recipes [recipe :crafting-time])))

(defn part-output-rate [recipe]
  (let [{:keys [output crafting-time]} (get parts-recipes recipe)]
    (/ output crafting-time)))

(defn raw-ingredient-output-rate [recipe]
  (or (get-in parts-recipes [recipe :output] 1/2)))

(defn output-rate [recipe]
  (if (part? recipe)
    (part-output-rate recipe)
    (raw-ingredient-output-rate recipe)))

(defn equilibrium
  "Ratio of equilibrium destination:source, i.e. 1/2 means source provides
  enough for two copies of destination building."
  [source destination]
  (/ (consumption-rate destination source)
     (output-rate source)))

(declare requirements)

(defn requirements*
  [recipe ingredient]
  (let [ratio  (equilibrium ingredient recipe)
        base   {:ratio ratio}]
    (if (part? ingredient)
      (assoc base :requirements (requirements ingredient))
      base)))

(defn requirements
  "Get the required ingredients to make a recipe.

  Recursive and will drill down to raw ingredients, not just required parts."
  [recipe]
  (let [ingredients (keys (get-in parts-recipes [recipe :ingredients]))]
    (reduce (fn [acc i]
              (assoc acc i (requirements* recipe i)))
            {}
            ingredients)))
