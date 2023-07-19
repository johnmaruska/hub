(ns hub.sketchbook.potionomics
  (:require
   [clojure.java.io :as io]
   [clojure.math.combinatorics :as combo]
   [clojure.string :as string]))

(def all-traits ["Taste" "Sensation" "Sound" "Visual" "Aroma"])
(def all-magimins ["A" "B" "C" "D" "E"])

(defn quality
  "Returns [quality-value stars]"
  [magimin-count]
  (condp <= magimin-count
    1125 [5 5]
    1040 [5 4]
    960  [5 3]
    875  [5 2]
    800  [5 1]
    720  [5 0]
    660  [4 5]
    620  [4 4]
    580  [4 3]
    545  [4 2]
    505  [4 1]
    470  [4 0]
    430  [3 5]
    400  [3 4]
    370  [3 3]
    345  [3 2]
    315  [3 1]
    290  [3 0]
    260  [2 5]
    235  [2 4]
    215  [2 3]
    195  [2 2]
    170  [2 1]
    150  [2 0]
    130  [1 5]
    115  [1 4]
    105  [1 3]
    90   [1 2]
    75   [1 1]
    60   [1 0]
    50   [0 5]
    40   [0 4]
    30   [0 3]
    20   [0 2]
    10   [0 1]
    0    [0 0]))

(defn recipe [name type ratios]
  {:name   name
   :type   type
   :ratios (zipmap all-magimins ratios)})

(def recipes
  (map #(apply recipe %1)
       [[:health :potion [1 1 0 0 0]]
        [:mana :potion [0 1 1 0 0]]
        [:stamina :potion [1 0 0 0 1]]
        [:speed :potion [0 0 1 1 0]]
        [:tolerance :potion [0 0 0 1 1]]
        [:fire :tonic [1 0 1 0 0]]
        [:ice :tonic [1 0 0 1 0]]
        [:thunder :tonic [0 1 0 1 0]]
        [:shadow :tonic [0 1 0 0 1]]
        [:radiation :tonic [0 0 1 0 1]]
        [:sight :enhancer [3 4 3 0 0]]
        [:alertness :enhancer [0 3 4 3 0]]
        [:insight :enhancer [4 3 0 0 3]]
        [:dowsing :enhancer [3 0 0 3 4]]
        [:seeking :enhancer [0 0 3 4 3]]
        [:poison :cure [2 0 1 1 0]]
        [:drowsiness :cure [1 1 0 2 0]]
        [:petrification :cure [1 0 2 0 1]]
        [:silence :cure [0 2 1 0 1]]
        [:curse :cure [0 1 1 0 2]]]))

(def ingredients
  (hub.util.file/load-edn (io/resource "data/potionomics_ingredients.edn")))

(def empty-ingredient
  {"Type" "Empty",
   "Ingredient" "Empty",
   "A" 0 "B" 0 "C" 0 "D" 0 "E" 0
   "Total" 0 "Rarity" 0 "Base Price" 0})

;; works on `(:ratios recipe)` or `ingredient`
(defn relevant-magimins [item]
    (->> (select-keys item all-magimins)
         (filter (fn [[k v]] (pos? v)))
         (map key)
         set))

;; Potentially would be a place for `clara` rules engine
(defn relevant-ingredients [max-magimins recipe traits]
  (let [required-magimins  (relevant-magimins (:ratios recipe))]
    (->> ingredients
         ;; Only look at ingredients that use only magimins for the
         ;; recipe
         (filter (fn [ingredient]
                   (clojure.set/subset? (relevant-magimins ingredient)
                                        required-magimins)))
         ;; Only look at ingredients that won't exceed max count
         (filter (fn [ingredient]
                   (<= (get ingredient "Total") max-magimins))))))


(defn sum-magimins [ingredients]
  (reduce (fn [acc ingredient]
            (-> acc
                (update "A" + (get ingredient "A"))
                (update "B" + (get ingredient "B"))
                (update "C" + (get ingredient "C"))
                (update "D" + (get ingredient "D"))
                (update "E" + (get ingredient "D"))
                (update "Total" + (get ingredient "Total"))))
          (select-keys empty-ingredient (conj all-magimins "Total"))
          ingredients))

(defn gcd
  ([] 0)
  ([a] a)
  ([a b]
   (.gcd (biginteger a) (biginteger b)))
  ([a b c]
   (gcd (gcd a b) c))
  ([a b c d]
   (gcd (gcd a b c) d)))

(defn total-magimins [ingredients]
  (get (sum-magimins ingredients) "Total"))

(defn ratios [ingredients]
  (let [summed          (sum-magimins ingredients)
        total           (get summed "Total")
        magimin-amounts (dissoc summed "Total")
        magimin-gcd     (apply gcd (remove zero? (vals magimin-amounts)))]
    (reduce (fn [acc [magimin amount]]
              (assoc acc magimin (if (zero? amount)
                                   0
                                   (/ amount magimin-gcd))))
            {} magimin-amounts)))

;;; naive implementation. all combinations, filter.
(defn optimal-piles [max-magimins max-ingredients recipe traits]
  (let [multiset (mapcat #(repeat max-ingredients %)
                         (conj (relevant-ingredients max-magimins recipe traits)
                               empty-ingredient))]
    (->> (combo/combinations multiset max-ingredients)
         (filter (fn [ingredients] (= max-magimins (total-magimins ingredients))))
         (filter (fn [ingredients] (= (:ratios recipe) (ratios ingredients)))))
    ))

(comment
  (def piles
    (take 20 (optimal-piles 240 7 (first recipes) [])))
  (->> piles
       (map (fn [ms] (map (fn [m] (get m "Ingredient"))
                          ms)))
       (map (fn [ingredients] (remove #(= "Empty" %1) ingredients))))


  (relevant-magimins ING)
  (->> ING (filter (fn [[k v]] (and (number? v) (pos? v)))))
  (relevant-magimins (:ratios (first recipes)))
  (-> (relevant-ingredients 150 (first recipes) [])
      (combo/count-combinations 3))

  )



(defn node? [x]
  (and (sequential? x)
       (keyword? (first x))
       (map? (second x))))

(defn body [node] (drop 2 node))

(defn tagged? [tag] (fn [node] (= tag (first node))))

(defn tbody [table]
  (first (filter (tagged? :tbody)
                 (body table))))

(defn cell-value [[_ _ & body]]
  (if (string? (first body))
    (clojure.string/trim-newline (first body))
    body))

(defn tr-values [[_ _ & cells]] (map cell-value cells))

(defn parsed-table->maps [table]
  (let [csv-data (->> table tbody body
                      (filter (tagged? :tr))
                      (map tr-values))]
    (hub.util.file/csv-data->maps csv-data identity)))

(defn trait [node]
  (let [[_ {:keys [alt]} & _]
        (->> (tree-seq node? body node)
             (filter (tagged? :img))
             first)]
    (cond
      (not (string? alt))               nil
      (string/includes? alt "positive") :positive
      (string/includes? alt "negative") :negative)))

(defn base-price [node]
  (-> node second string/trim-newline Integer/parseInt))

(defn coerce-ingredient [m]
  (as-> {} $
    (reduce (fn [acc k] (assoc acc k (trait (get m k)))) $ all-traits)
    (reduce (fn [acc k] (assoc acc k (Integer/parseInt (get m k)))) $
            (concat all-magimins ["Rarity" "Total"]))
    (reduce (fn [acc k] (assoc acc k (get m k))) $ ["Type" "Ingredient"])
    (assoc $ "Base Price" (base-price (get m "Base Price")))))


(comment
  (require '[pl.danieljanus.tagsoup :as html])
  (def parsed-html (html/parse "https://potionomics.fandom.com/wiki/Ingredients"))
  (def table
    (->> (tree-seq node? body parsed-html)
         (filter (tagged? :table))
         first))
  (-> table tbody body second body (nth 15) cell-value)
  (-> (parsed-table->maps table) first (get "Base Price") base-price)
  (->> table parsed-table->maps (map coerce-ingredient) vec (spit (io/resource "data/potionomics_ingredients.edn")))
  )
