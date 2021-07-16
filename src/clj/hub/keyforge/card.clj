(ns hub.keyforge.card)

(defn remove-card [xs card]
  (filter #(not= (:id card) (:id %)) xs))

(let [counter (atom 0)]
  (defn id [card]
    (swap! counter inc)
    (assoc card :id @counter)))


(def example
  {:id     1
   :name   "FooBar"
   :house  "Example"
   :ready? false
   :play   nil
   :deploy false})

(defn escotera []
  (id
   {:name "Dr. Escotera"
    :house "Logos"}))

(defn quixo []
  (id
   {:name "Quixo the \"Adventurer\""
    :house "Logos"}))
