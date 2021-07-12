(ns hub.bio-test
  (:require [hub.bio :as sut]
            [clojure.test :as t :refer [deftest is testing]]))

(defmacro swallow-exception
  {:style/indent 1}
  [& body]
  `(try ~@body (catch Exception ex# nil)))

(deftest amino-acid
  (testing "produces an answer for all triplets"
    (for [primary   [\T \C \A \G]
          secondary [\T \C \A \G]
          tertiary  [\T \C \A \G]]
      (let [input   (list primary
                          secondary
                          tertiary)
            results (try
                      (sut/amino-acid input)
                      (catch Exception ex
                        {:input input}))]
        (is (every? keyword? results))))))
