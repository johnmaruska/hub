(ns hub.discljord.commands
  (:require
   [clojure.string :as string]
   [hub.discljord.admin :as admin]
   [hub.discljord.guess-that-sound :as guess-that-sound]
   [hub.discljord.minesweeper :as minesweeper]
   [hub.discljord.tarot :as tarot]))

(def prefix
  {;; Admin
   "!bot please be kill"    (fn [& _]
                              (throw (ex-info "ManualKill" {:manual-kill? true})))
   "!working"               #'admin/working
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
