(ns user
  (:require [figwheel.main]))

(defn init! []
  (figwheel.main/start :dev))
