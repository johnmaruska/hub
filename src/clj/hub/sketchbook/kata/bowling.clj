(ns hub.kata.bowling)

(def standard-game
  (repeat 20 4))  ; 80

(def perfect-game
  (repeat 12 10))  ; 300

;;; caring about nils makes this a lot more complicated than it needs to be
;; just remove the nils and count rolls

;;;;;;;;;;;;;;;;;;;;;;;;;

(defn strike? [rolls]
  (= 10 (first rolls)))

(defn spare? [rolls]
  (and (not (strike? rolls))
       (= 10 (+ (first rolls) (second rolls)))))

(defn score-game [rolls]
  (loop [score 0
         rem-rolls rolls
         frame 0]
    (cond
      (= 10 frame)
      score

      (strike? rem-rolls)
      (recur (apply + score (take 3 rem-rolls))
             (drop 1 rem-rolls)
             (inc frame))

      (spare? rem-rolls)
      (recur (apply + score (take 3 rem-rolls))
             (drop 2 rem-rolls)
             (inc frame))

      :else
      (recur (apply + score (take 2 rem-rolls))
             (drop 2 rem-rolls)
             (inc frame)))))
