(ns hub.conway.output.sketch
  (:require
   [quil.core :as q]
   [quil.middleware :as m]
   [hub.conway.game :as game]
   [hub.conway.seed :as seed]
   [hub.util.grid :as util]))

;;; colors
(def white 255)
(def black 0)
;;; properties
(def alive white)
(def dead  black)
;;; param properties
(def cell-size 25)
(def frame-rate 5)

(defn- setup
  [seed-grid frame-rate]
  (q/frame-rate frame-rate)
  (q/fill alive)
  {:grid seed-grid})

(defn- update-state [state]
  (update state :grid game/step-grid))

(defn- draw-state
  [state]
  (let [top-pixel  0
        left-pixel 0]
    (q/background dead)  ; clear sketch
    (->> (util/get-coord-objs (:grid state))
         (filter (comp game/alive? :value))
         (run! (fn [{:keys [row col value]}]
                 (q/rect (+ left-pixel (* col cell-size))
                         (+ top-pixel  (* row cell-size))
                         cell-size cell-size))))))

(defn sketch
  [grid]
  (let [{:keys [x y]} (util/get-dimensions grid)
        grid-size     [x y]
        sketch-size   (mapv #(* cell-size %) grid-size)]
    (q/sketch
     ;;; basic properties
     :title    "Conway's Game of Life"
     :features [:keep-on-top]
     :size     sketch-size
     ;;; lifecycle fns
     :setup  #(setup grid frame-rate)
     :update update-state
     :draw   draw-state
     ;;; extra things, TODO look into
     :middleware [m/fun-mode])))


#_
(-> (seed/all-dead 50 50)
    (seed/overlay seed/glider 5 5)
    sketch)
