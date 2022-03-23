(ns hub.transactions-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [hub.transactions :as sut]))

(deftest process-tx
  (testing "removes prefix"
    (is (= {:Description "just this left"
            :Category    :uncategorized}
           (sut/process-tx {:Description "VENDOR *just this left"}
                           {:prefixes ["VENDOR *"]}))))
  (testing "adds category"
    (is (= {:Description "gas station" :Category :car}
           (sut/process-tx {:Description "gas station"}
                           {:categories {:home #{"IKEA"}
                                         :car  #{"gas"}}})))))
