(ns hub.sketchbook.keyforge.card)

(defn remove-card [xs card]
  (filter #(not= card %) xs))

(defn exhaust! [card]
  (swap! card assoc :ready? false))


(def card-defaults
  {;;; generic stats
   :ready? false
   ;;; card abilities
   :enters-ready false
   :play   nil
   :fight  nil
   :reap   nil
   :deploy false})

(defn make-card [m]
  (atom (merge card-defaults m)))

(defn escotera []
  (atom
   {:name "Dr. Escotera"
    :house "Logos"}))

(defn quixo []
  (atom
   {:name "Quixo the \"Adventurer\""
    :house "Logos"}))
