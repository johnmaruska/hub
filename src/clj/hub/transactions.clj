(ns hub.transactions
  (:require [hub.util.data-file :refer [load-csv load-edn]]
            [clojure.string :as string]))

(defn desc-start? [tx substr]
  (string/starts-with? (:Description tx) substr))

(defn remove-prefix [tx]
  (let [strip-prefix (fn [s prefix]
                       (if (string/starts-with? s prefix)
                         (string/replace-first s (re-pattern prefix) "")
                         s))]
    (reduce (fn [acc prefix]
              (update acc :Description #(strip-prefix % (str prefix "  "))))
            tx (load-edn "prefixes.edn"))))

(defn categorize* [tx]
  (->> (for [[category members] (load-edn "categories.edn")]
         (for [member members]
           (when (string/starts-with? (:Description tx) member)
             category)))
       flatten
       (filter identity)
       first))

(defn categorize [tx]
  (assoc tx :Category (or (when (seq (:Credit tx)) :income)
                          (categorize* tx)
                          :uncategorized)))

(defn total [txs]
  (let [net-change (fn [tx] (if (seq (:Debit tx))
                              (- (Float/parseFloat (:Debit tx)))
                              (+ (Float/parseFloat (:Credit tx)))))]
    (reduce + (map net-change txs))))

(defn totals [categorized-txs]
  (reduce (fn [acc [category txs]]
            (assoc acc category (total txs)))
          {}
          categorized-txs))

(defn yearly-breakdown [txs]
  (totals (group-by :Category txs)))

(defn leading-zero [s]
  (if (= 1 (count s)) (str "0" s) s))
(defn month [tx]
  (let [[month _ year] (string/split (:Date tx) #"/")]
    (str year "-" (leading-zero month))))

(defn update-all [f m]
  (reduce (fn [acc [k xs]]
            (assoc acc k (f xs)))
          {} m))

(defn monthly-breakdown [txs]
  (->> txs
       (group-by month)
       (update-all #(group-by :Category %))
       (update-all totals)))

(defn category-over-time [txs category]
  (->> txs
       (filter #(= category (:Category %)))
       (group-by month)
       totals
       sort))


(comment
  (def txs
    (->> (load-csv "transactions.csv")
         #_(filter (comp empty? :Credit))  ; only care about spending
         (map remove-prefix)
         (map categorize)))
  (let [mandatory-categories [:donation
                              :income
                              :groceries
                              :donation
                              :bills
                              :home
                              :car]]
    (->> (monthly-breakdown txs)
         (update-all (fn [xs]
                       (->> (map #(get xs % 0) mandatory-categories)
                            (reduce +))))
         sort))

  (-> (monthly-breakdown txs)
      (get "2021-08"))

  (->> txs
       (filter #(= "2021-08" (month %)))
       (filter #(= :home (:Category %))))
  )
