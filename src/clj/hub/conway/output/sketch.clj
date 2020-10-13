(ns hub.conway.output.sketch
  (:require
   [quil.core :as q]
   [quil.middleware :as m]
   [hub.conway.game :as game]
   [hub.conway.seed :as seed]
   [hub.conway.util :as util]))

(defn setup [seed-grid]
  (q/frame-rate 5)
  (q/color-mode :rgb)
  ;; pick seed board
  {:grid seed-grid})

(defn update-state [state]
  (update state :grid game/step-grid))

(def cell-size 50)
(defn draw-state [state]
  ;; TODO is default origin top-left or bottom-left?
  (let [top-pixel  0
        left-pixel 0]
    (q/background 0)
    (q/fill 255)
    (->> (util/get-coords (:grid state))
         (filter (comp game/alive? :value))
         (run! (fn [{:keys [row col value]}]
                 (q/rect (+ left-pixel (* col cell-size))
                         (+ top-pixel  (* row cell-size))
                         cell-size cell-size))))))

(def initial-grid
  (-> (seed/all-dead 10 10)
      (seed/overlay seed/glider)))

(q/defsketch sketch
  :title "Conway's Game of Life"
  :update update-state
  :setup (partial setup initial-grid)
  :draw draw-state
  :size [500 500]
  :features [:keep-on-top]
  :middleware [m/fun-mode])
