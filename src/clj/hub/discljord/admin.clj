(ns hub.discljord.admin
  (:require [hub.discljord.util :as util]
            [clojure.string :as string]))

(defn handle [bot event]
  (util/command (:content event)
    "!working"
    (util/reply bot event "https://giphy.com/gifs/9K2nFglCAQClO")))
