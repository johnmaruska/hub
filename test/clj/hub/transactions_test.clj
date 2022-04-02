(ns hub.transactions-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [hub.transactions :as sut]))

(def unformatted-date "02/20/2022")
(def formatted-date "2022-02-20")
(def prefix "VENDOR *")
(def base-description "gas station")
(def description (str prefix base-description))

(deftest category
  (testing "returns first member that matches starting of tx string"
    (is (= :expected (sut/category {:Description "an expected value"}
                                   {:categories {:expected #{"an expected"}}}))))
  (testing "works with regular expressions. Matches only beginning"
    (is (= :expected (sut/category {:Description "0123 expected"}
                                   {:categories {:expected #{"[0-3]* expected"}}})))))

(deftest process-tx
  (testing "removes prefix"
    (let [result (sut/process-tx {:Description description :Date unformatted-date}
                                 {:prefixes [prefix "some other"]})]
      (is (= base-description (:Description result)))))
  (testing "adds category"
    (let [result (sut/process-tx {:Description base-description :Date unformatted-date}
                                 {:categories {:home #{"IKEA"} :car #{"gas"}}})]
      (is (= :car (:Category result)))))
  (testing "formats date"
    (let [result (sut/process-tx {:Description description :Date unformatted-date} {})]
      (is (= formatted-date (:Date result))))))

(deftest month
  (testing "extracts YYYY-MM format from `tx` date"
    (is (= "2022-02" (sut/month {:Date "2022-02-20"})))))

(deftest remove-categories
  (testing "removes two categories"
    (is (= [{:Category :third}]
           (sut/remove-categories [{:Category :first}
                                   {:Category :second}
                                   {:Category :third}]
                                  :first :second)))))

(deftest remove-shared-transactions
  (testing "removes transfers from checking to savings. Identified by category"
    (let [savings  [{:Date "2021-28-05" :Debit "" :Credit "0.05" :Category :banking}
                    {:Date "2021-05-17" :Debit "" :Credit "574.79" :Category :transfers-from-checking}]
          checking [{:Date "2021-05-17" :Debit "574.79" :Credit "" :Category :transfers-to-savings}
                    {:Date "2021-07-26" :Debit "14.22" :Credit "" :Category :groceries}]]
      (is (= {:savings  [{:Date "2021-28-05" :Debit "" :Credit "0.05" :Category :banking}]
              :checking [{:Date "2021-07-26" :Debit "14.22" :Credit "" :Category :groceries}]}
             (sut/remove-shared-transactions checking savings)))))
  (testing "removes transfers from savings to checking. Have to match."
    (let [savings  [{:Date "2021-07-26" :Debit "300.00" :Credit "" :Category :transfers-out-of-savings}
                    {:Date "2021-28-05" :Debit "" :Credit "0.05" :Category :banking}]
          checking [{:Date "2021-07-26" :Debit "" :Credit "300.00" :Category :transfers-from-savings}
                    {:Date "2021-07-26" :Debit "14.22" :Credit "" :Category :groceries}]]
      (is (= {:savings  [{:Date "2021-28-05" :Debit "" :Credit "0.05" :Category :banking}]
              :checking [{:Date "2021-07-26" :Debit "14.22" :Credit "" :Category :groceries}]}
             (sut/remove-shared-transactions checking savings))))))
