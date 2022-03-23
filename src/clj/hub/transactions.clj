(ns hub.transactions
  (:require [hub.util.data-file :refer [load-csv load-edn]]
            [clojure.string :as string]
            [hub.util :as util]))

;;; Transformations on the original CSV

(defn desc-start?
  "Does the `tx` have a description which starts with `substr`?"
  [tx substr]
  (string/starts-with? (:Description tx) substr))

(defn remove-prefix
  "Remove all prefixes from string, in order."
  [s {:keys [prefixes]}]
  (reduce (fn [acc prefix]
            (util/remove-prefix acc prefix))
          s prefixes))

(defn leading-zero
  "Add a leading zero to force two digits."
  [s]
  (if (= 1 (count s)) (str "0" s) s))

(defn date->iso8061 [date]
  (let [[month day year] (string/split date #"/")]
    (str year "-" (leading-zero month) "-" (leading-zero day))))

(defn category
  "Determine category for `tx` as specified by groupings in config."
  [tx {:keys [categories]}]
  (or (when (seq (:Credit tx)) :income)
      (->> (for [[category members] categories]
             (for [member members]
               (when (string/starts-with? (:Description tx) member)
                 category)))
           flatten (filter identity) first)
      :uncategorized))

(defn process-tx
  "Format existing values and derive new values for `tx` map"
  [tx config]
  (let [formatted (-> tx
                      (update :Description #(remove-prefix % config))
                      (update :Date date->iso8061))]
    (assoc formatted :Category (category formatted config))))


;;;; Mostly helpful for crawling on my own

(defn total
  "Get net change to account for transaction."
  [txs]
  (letfn [(net-change [tx]
            (if (seq (:Debit tx))
              (- (Float/parseFloat (:Debit tx)))
              (+ (Float/parseFloat (:Credit tx)))))]
    (reduce + (map net-change txs))))

(defn totals
  "Get totals for each `txs`' category."
  [categorized-txs]
  (reduce (fn [acc [category txs]]
            (assoc acc category (total txs)))
          {}
          categorized-txs))

(defn yearly-breakdown
  "We only get data for a year so just don't break down."
  [txs]
  (totals (group-by :Category txs)))


(defn month
  "Parse the `tx` date to YYYY-MM format."
  [tx]
  (let [[year month _] (string/split (:Date tx) #"-")]
    (str year "-" (leading-zero month))))

(defn monthly-breakdown [txs]
  (->> txs
       (group-by month)
       (util/update-vals #(group-by :Category %))
       (util/update-vals totals)))

(defn category-over-time [txs category]
  (->> txs
       (filter #(= category (:Category %)))
       (group-by month)
       totals
       sort))

;;; Merging checking and savings

(defn remove-categories
  [txs & categories]
  (->> categories
       (apply dissoc (group-by :Category txs))
       vals
       (apply concat)))

(defn amounts-match [sender recipient]
  (= (:Debit sender) (:Credit recipient)))

(defn remove-shared-transactions
  "Remove all transactions which take place checking<-->savings."
  [checking savings]
  (let [from-savings-by-date    (->> checking
                                     (group-by :Category)
                                     :transfers-from-savings
                                     (group-by :Date))
        transfers-not-shared    (->> savings
                                     (group-by :Category)
                                     :=transfers-out-of-savings
                                     (remove (fn [tx-s]
                                               (some (fn [tx-c] (amounts-match tx-s tx-c))
                                                     (get from-savings-by-date (:Date tx-s))))))]
    {:savings  (->> (remove-categories savings :transfers-out-of-savings :transfers-from-checking)
                    (concat transfers-not-shared))
     :checking (remove-categories checking :transfers-from-savings :transfers-to-savings)}))

(defn merge-histories
  "Create one large list of transactions, removing both sides of a transfer between the two accounts."
  [checking savings]
  (let [{:keys [checking savings]} (remove-shared-transactions checking savings)]
    (concat (map #(assoc % :account :savings) savings)
            (map #(assoc % :account :checking) checking))))

(comment
  (def config {:categories (load-edn "categories.edn")
               :prefixes   (sort-by count > (load-edn "prefixes.edn"))})
  )
