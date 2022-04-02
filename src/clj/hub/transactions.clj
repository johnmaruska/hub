(ns hub.transactions
  "Functions for inspecting bank transactions in CSV format.

  Hopefully build this out into a visualization component later?"
  (:require [hub.util.data-file :as data]
            [clojure.string :as string]
            [hub.util :as util]))

;;; Transformations on the original CSV

(defn remove-prefix
  "Remove prefixes from `tx` description, loaded from `prefixes.edn`."
  [s prefixes]
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
  "Determine category for `tx` as specified by groupings in config.

  Entries in config must be either a string prefix, or a parse-able regex.
  Regexes will apply to the beginning of the string (no need for ^ )

  I had issues with escaping whitespace in the regex. This gave a parse error
  because s is not a valid escape character. \\s gives a different error in that
  it just shows as a literal? Not sure what that's about. I just avoided it."
  [tx {:keys [categories]}]
  (or (->> (for [[category members] categories]
             (for [member members]
               (when (or (string/starts-with? (:Description tx) member)
                         (re-find (re-pattern (str "^" member)) (:Description tx)))
                 category)))
           flatten (filter identity) first)
      :uncategorized))

(defn process-tx
  "Format existing values and derive new values for `tx` map"
  [tx config]
  (let [formatted (-> tx
                      (update :Description #(remove-prefix % (:prefixes config)))
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


(defn match-keys [m k]
  (if (keyword? (first (keys m))) k (name k)))

(defn totals
  "Get totals for each `txs`' category."
  [categorized-txs]
  (let [results (reduce (fn [acc [category txs]]
                          (assoc acc category (total txs)))
                        {}
                        categorized-txs)
        net     (reduce + (vals results))]
    (assoc results (match-keys results :net) net)))

(defn month
  "Parse the `tx` date to YYYY-MM format."
  [tx]
  (let [[year month _] (string/split (:Date tx) #"-")]
    (str year "-" (leading-zero month))))

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

(def base-config
  {:categories (data/load-edn "categories.edn")
   :prefixes   (sort-by count > (data/load-edn "prefixes.edn"))})

(defn load-csv
  ([csv]
   (load-csv csv base-config))
  ([csv config]
   (map #(process-tx % config) (data/load-csv csv))))

(comment
  (do
    (def savings  (load-csv "savings.csv"))
    (def checking (load-csv "checking.csv")))

  (->> (merge-histories checking savings)
       (group-by :Category)))
