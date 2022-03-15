(ns hub.conway
  (:require
   [hub.conway.game :as game]
   [hub.conway.output.sketch :as sketch]
   [hub.conway.output.terminal :as terminal]
   [hub.conway.seed :as seed]))

(def delay-ms 200)
(def default-height 10)
(def default-width 10)

(defn sketch-animate
  "Graphically display game animation."
  ([]
   (let [seed (seed/random default-width default-height)]
     (sketch/animate seed game/play-round delay-ms)))
  ([seed]
   (sketch/animate seed game/play-round delay-ms)))

(defn terminal-animate
  "Display game state in the terminal, ASCII animation.
  Blocking, wrap in thread to unblock."
  ([]
   (let [seed (seed/random default-width default-height)]
     (terminal/animate seed game/play-round delay-ms)))
  ([seed]
   (terminal/animate seed game/play-round delay-ms)))

(defn autoplay
  "Generate a lazy-sequence of game states from `seed`.
  If no `seed` provided, randomly generate an initial state."
  ([]
   (autoplay (seed/random default-width default-height)))
  ([seed]
   (iterate game/play-round seed)))
