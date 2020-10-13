(ns hub.conway.output.sketch
  (:require
   [quil.core :as q]
   [quil.middleware :as m]
   [hub.conway.game :as game]
   [hub.conway.seed :as seed]
   [hub.conway.util :as util]))

;;; colors
(def white 255)
(def black 0)
;;; properties
(def fill white)
(def background black)
;;; param properties
(def default-cell-size 25)
(def default-frame-rate 5)

(defn- setup
  [seed-grid frame-rate]
  (q/frame-rate frame-rate)
  (q/fill fill)
  {:grid seed-grid})

(defn- update-state [state]
  (update state :grid game/step-grid))

(defn- draw-state
  [state]
  (let [top-pixel  0
        left-pixel 0]
    (q/background background)  ; clear sketch
    (->> (util/get-coords (:grid state))
         (filter (comp game/alive? :value))
         (run! (fn [{:keys [row col value]}]
                 (q/rect (+ left-pixel (* col cell-size))
                         (+ top-pixel  (* row cell-size))
                         cell-size cell-size))))))

(defn sketch
  [grid & {:keys [cell-size frame-rate]
           :or   {cell-size  default-cell-size
                  frame-rate default-frame-rate}}]
  (let [{:keys [x y]} (game/get-dimensions grid)
        grid-size     [x y]
        sketch-size   (mapv #(* cell-size %) grid-size)]
    (q/sketch
     ;;; basic properties
     :title    "Conway's Game of Life"
     :features [:keep-on-top]
     :size     sketch-size
     ;;; lifecycle fns
     :setup  #(setup initial-grid frame-rate)
     :update update-state
     :draw   draw-state
     ;;; extra things, TODO look into
     :middleware [m/fun-mode])))
