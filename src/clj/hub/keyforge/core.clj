(ns hub.keyforge.core)

;; TODO: current-player -> active-player
;; TODO: [:turn :house] -> [:turn :active-house]
(def danova
  {:name "Danova, Port Demolisher"
   :houses ["Brobnar" "Shadows" "Logos"]
   :decklist [0 1 2 3 4 5 6 7 8 9]})

(def turn
  {:current-player :alpha
   :house          "Brobnar"
   })
