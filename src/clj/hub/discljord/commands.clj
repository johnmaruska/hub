(ns hub.discljord.commands
  (:require
   [clojure.string :as string]
   [hub.discljord.guess-that-sound :as guess-that-sound]
   [hub.discljord.minesweeper :as minesweeper]
   [hub.discljord.tarot :as tarot]
   [hub.discljord.util :as util]))

(defn manual-kill [& _]
  ;; this works but why does it label itself a syntax error?
  (throw (ex-info "ManualKill" {:manual-kill? true})))

(defn working [bot event]
  (util/reply bot event "https://giphy.com/gifs/9K2nFglCAQClO"))

(def prefix
  {;; Admin
   "!bot please be kill"    #'manual-kill
   "!working"               #'working
   ;; Guess that sound
   "!play guess-that-sound" #'guess-that-sound/start!
   "!guess"                 #'guess-that-sound/guess!
   "!answer"                #'guess-that-sound/answer!
   ;; Misc
   "!minesweep"             #'minesweeper/spit-board
   "!tarot"                 #'tarot/handle-event})

(defn matching-prefixes [event]
  (filter #(string/starts-with? (:content event) %)
          (keys prefix)))
