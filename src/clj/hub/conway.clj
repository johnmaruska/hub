(ns hub.conway
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [hub.conway.game :as game]
   [hub.conway.sketch :as sketch]
   [hub.conway.terminal :as terminal]
   [hub.util.file :as file]))

(def delay-ms 200)
(def default-height 10)
(def default-width 10)

(defn random-seed []
  (game/random-seed default-width default-height))

(defn load-seed [seed-name]
  (file/load-edn (io/resource (str "data/conway/" seed-name ".edn"))))

(defn sketch-animate
  "Graphically display game animation."
  ([]
   (sketch/animate (random-seed) game/play-round delay-ms))
  ([seed]
   (sketch/animate seed game/play-round delay-ms)))

(defn terminal-animate
  "Display game state in the terminal, ASCII animation.
  Blocking, wrap in thread to unblock."
  ([]
   (terminal/animate (random-seed) game/play-round delay-ms))
  ([seed]
   (terminal/animate seed game/play-round delay-ms)))

(defn autoplay
  "Generate a lazy-sequence of game states from `seed`.
  If no `seed` provided, randomly generate an initial state."
  ([]
   (autoplay (random-seed)))
  ([seed]
   (iterate game/play-round seed)))

(defn main [command & [seed-name]]
  (let [seed (if seed-name
               (load-seed seed-name)
               (random-seed))]
    (case command
      "sketch" (sketch-animate seed)
      "print"  (terminal-animate seed)
      (log/error "conway subcommand must be one of [sketch print]"))))
