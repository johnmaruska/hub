(ns hub.discljord.admin
  (:require [hub.discljord.util :as util]
            [clojure.string :as string]))

(defn working [bot event]
  (util/reply bot event "https://giphy.com/gifs/9K2nFglCAQClO"))
