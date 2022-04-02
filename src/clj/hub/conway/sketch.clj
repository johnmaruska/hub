(ns hub.conway.sketch
  (:require
   ;; com.apple.eawt.QuitHandler ClassNotFoundException is because quil
   ;; wraps Processing which requires Java v8, not higher
   [quil.core :as q]
   [quil.middleware :as m]
   [hub.conway.game :as game]
   [hub.util.grid :as grid]))

;;; colors
(def white 255)
(def black 0)
;;; properties
(def alive white)
(def dead  black)
;;; param properties
(def cell-size 25)

(defn- setup
  [seed-grid frame-rate]
  (q/frame-rate frame-rate)
  (q/fill alive)
  {:grid seed-grid})

(defn- draw-state
  [state]
  (let [top-pixel  0
        left-pixel 0]
    (q/background dead)  ; clear sketch
    (->> (grid/coord-maps (:grid state))
         (filter (comp game/alive? :value))
         (run! (fn [{:keys [row col]}]
                 (q/rect (+ left-pixel (* col cell-size))
                         (+ top-pixel  (* row cell-size))
                         cell-size cell-size))))))

;; TODO: check if this is blocking, probably not with quil right?
(defn animate
  [grid update-fn delay-ms]
  (let [{:keys [x y]} (grid/dimensions grid)
        grid-size     [x y]
        sketch-size   (mapv #(* cell-size %) grid-size)
        frame-rate    (/ 1000 delay-ms)]
    (q/sketch
     ;;; basic properties
     :title    "Conway's Game of Life"
     :features [:keep-on-top]
     :size     sketch-size
     ;;; lifecycle fns
     :setup  #(setup grid frame-rate)
     :update #(update % :grid update-fn)
     :draw   draw-state
     ;;; extra things, TODO look into
     :middleware [m/fun-mode])))
