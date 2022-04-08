(ns user
  (:require
   [mount.core :as mount]
   [clojure.java.io :as io]))

(defn mount-restart []
  (mount/stop)
  (mount/start))
