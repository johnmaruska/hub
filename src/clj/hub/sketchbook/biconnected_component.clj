(ns hub.sketchbook.biconnected-component)

(def nodes (into #{} (range 1 15)))
(def edges
  #{#{1 2} #{1 3}
    #{2 4} #{3 4}
    #{4 5}
    #{5 6}
    #{6 7}
    #{7 8} #{7 9} #{9 11}
    #{9 10}
    #{7 12} #{11 13} #{12 13}
    #{12 14} #{13 14}})

(def G
  {:V nodes
   :E edges})

(defn adjacent
  "Return vertices adjacent to `vertex`."
  [graph vertex]
  (reduce (fn [acc edge]
            (cond
              (= vertex (first edge)) (conj acc (second edge))
              (= vertex (second edge)) (conj acc (first edge))
              :else acc))
          []
          (graph :E)))

(defmacro manage-dfs-visit [u state & body]
  `(let [assign!# (partial swap! ~state assoc-in)]
     (assign!# [:time] (inc (:time @~state)))
     (assign!# [~u :d] (:time @~state))
     (assign!# [~u :color] :gray)
     ~@body
     (assign!# [:time] (inc (:time @~state)))
     (assign!# [~u :color] :black)
     (assign!# [~u :f] (:time @~state))))

(defn init-state [g]
  (reduce (fn [acc v]
            (assoc-in acc [v :color] :white))
          {:time 0} (g :V)))


;; https://codeforces.com/blog/entry/71146
(defn articulation-points-dfs [g u state]
  (let [children (atom 0)
        assign! (partial swap! state assoc-in)]
    (manage-dfs-visit
     u state
     (assign! [u :low] (:time @state))
     (doseq [v (adjacent g u)]
       (cond
         (= v (:parent u)) nil

         (not= :white (get-in @state [v :color]))
         (assign! [u :low] (min (get-in @state [u :low])
                                (get-in @state [v :d])))
         :else
         (do
           (swap! children inc)
           (assign! [v :parent] u)
           (articulation-points-dfs G v state)
           (when (<= (get-in @state [u :d])
                     (get-in @state [v :low]))
             (assign! [u :ap] true))
           (assign! [u :low] (min (get-in @state [u :low])
                                  (get-in @state [v :low])))))))
    @children))

(defn articulation-points [g]
  (let [state (atom (init-state g))]
    (doseq [v (g :V)]
      (when (= :white (get-in @state [v :color]))
        (swap! state assoc-in [v :ap] (< 1 (articulation-points-dfs g v state)))))
    (->> @state (filter #(:ap (second %))) (map first))))

(into #{} (articulation-points G))  ; => #{4 5 6 7 9}
