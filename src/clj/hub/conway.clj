(ns hub.conway
  (:require
   [clojure.core.async :refer [thread]]
   [hub.conway.game :as game]
   [hub.conway.output.sketch :as sketch]
   [hub.conway.output.terminal :as terminal]
   [hub.conway.seed :as seed]))

(def delay-ms 200)
(def default-height 10)
(def default-width 10)

(defn animate
  "Graphically display game animation."
  ([]
   (animate (seed/random default-width default-height)))
  ([seed]
   (sketch/animate seed game/play-round delay-ms)))

(defn console-print
  ([]
   (console-print (seed/random default-width default-height)))
  ([seed]
   (thread
     (terminal/animate seed game/play-round delay-ms))))

(defn autoplay
  ([]
   (autoplay (seed/random default-width default-height)))
  ([seed]
   (iterate game/play-round seed)))

#_
(-> (seed/all-dead 50 50)
    (seed/overlay seed/glider 5 5)
    animate)
