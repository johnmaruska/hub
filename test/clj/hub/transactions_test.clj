(ns hub.transactions-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [hub.transactions :as sut]))

(def unformatted-date "02/20/2022")
(def formatted-date "2022-02-20")
(def prefix "VENDOR *")
(def base-description "gas station")
(def description (str prefix base-description))

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
