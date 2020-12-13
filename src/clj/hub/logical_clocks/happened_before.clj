(ns hub.logical-clocks.happened-before
  "Basic implementation of Figure 1 happened-before relation in ACM's
  Why Logical Clocks are Easy  https://queue.acm.org/detail.cfm?id=2917756

  This is useful pretty much exclusively for full-knowledge analysis, and not
  very efficient for that. Best used for illustration not computation.")

(defn init-node! [state nid]
  (-> state
      (assoc-in [:goes-to nid 1] [])
      (assoc-in [:node nid] {:nid nid :version 1})))

(defn init-event! [state [nid version]]
  (-> state
      (assoc-in [:goes-to nid version] [])
      (assoc-in [:node nid :version] version)))

(defn happened-before! [state src-event dest-event]
  (let [[src-nid src-ver] src-event]
    (-> state
        (init-event! dest-event)
        (update-in [:goes-to src-nid src-ver]
                   conj dest-event))))

(defn send! [state src-nid dest-nid]
  (let [dest-version (:version (get-in state [:node dest-nid]))
        src-event    [src-nid  (:version (get-in state [:node src-nid]))]
        dest-event   [dest-nid dest-version]
        result-event [dest-nid (inc dest-version)]]
    (-> state
        (happened-before! src-event  result-event)
        (happened-before! dest-event result-event))))

(defn bump! [state nid]
  (let [version    (:version (get-in state [:node nid]))
        src-event  [nid version]
        dest-event [nid (inc version)]]
    (happened-before! state src-event dest-event)))

(defn goes-to? [state start-event end-event]
  (if (= end-event start-event)
    true
    (some #(goes-to? state % end-event)
          (get-in (:goes-to state) start-event))))

(defn caused-by? [state end-event start-event]
  (goes-to? state start-event end-event))


(def example-state
  {:node    {:a {:nid     :a
                 :version 3}
             :b {:nid     :b
                 :version 3}
             :c {:nid     :c
                 :version 3}}
   :goes-to {:a {1 [[:a 2]]
                 2 [[:a 3] [:b 2]]
                 3 []}
             :b {1 [[:b 2]]
                 2 [[:b 3]]
                 3 [[:c 3]]}
             :c {1 [[:c 2]]
                 2 [[:c 3]]
                 3 []}}})

(defn verify []
  (-> (reduce init-node! {} [:a :b :c])
      (bump! :a)
      (send! :a :b)
      (bump! :c)
      (bump! :a)
      (bump! :b)
      (send! :b :c)
      (goes-to? [:a 1] [:c 3])))
