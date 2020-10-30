(ns hub.discljord.minesweeper
  (:require
   [clojure.string :as string]
   [hub.discljord.util :as util]
   [hub.minesweeper :as game]))

(def output
  {1 ":one:"
   2 ":two:"
   3 ":three:"
   4 ":four:"
   5 ":five:"
   6 ":six:"
   7 ":seven:"
   8 ":eight:"
   9 ":nine:"
   0 ":zero:"
   game/BOMB ":burn:"})

(defn discord-fmt [grid]
  (letfn [(fmt-row [row]
            (apply str (map #(util/spoiler (get output %)) row)))]
    (string/join "\n" (vec (map fmt-row grid)))))

(defn parse [contents]
  (when-let [matches (->> contents
                          (re-matcher #"!minesweeper (\d+)x(\d+) (\d+)")
                          re-find)]
    (let [[_ width height bombs] matches]
      {:width  (Integer/parseInt width)
       :height (Integer/parseInt height)
       :bombs  (Integer/parseInt bombs)})))

(defn handle [bot event]
  (when-let [{:keys [height width bombs]} (parse (:content event))]
    (let [grid (game/create height width bombs)]
      (util/reply bot event (discord-fmt grid)))))
