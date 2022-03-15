(ns hub.discljord.admin
  (:require [hub.discljord.util :as util]))

(def canned-reply
  {:working "https://giphy.com/gifs/9K2nFglCAQClO"})

(defn working [bot event]
  (util/reply bot event (:working canned-reply)))
