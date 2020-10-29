(ns hub.discljord.util-test
  (:require [hub.discljord.util :as util]
            [clojure.test :refer [deftest is testing]]))

(deftest command
  (testing "expands properly"
    (is (= :foofoo
           (util/command "!foo bar baaz"
               "!foo" :foofoo
               "!bar" :barbarian
               "!baaz" :bazaar))))
  (testing "can drop to lower cases"
    (is (= :bazaar
           (util/command "!baaz bar foo"
               "!foo" :foofoo
               "!bar" :barbarian
               "!baaz" :bazaar))))
  (testing "default nil behavior"
    (is (nil? (util/command "!bonk bonk"
                  "!foo" :foofoo
                  "!bar" :barbarian
                  "!baaz" :bazaar)))))
