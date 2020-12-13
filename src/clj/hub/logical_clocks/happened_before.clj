(ns hub.logical-clocks.happened-before
  "Basic implementation of Figure 1 happened-before relation
  in ACM Why Logical Clocks are Easy
  https://queue.acm.org/detail.cfm?id=2917756")

(def node
  {:nid     :a
   :version 3})

;; if this weren't a purely illustrative thing, I'd make this history
;; just a map that passes in, but this was easier to hack
(def goes-to (atom {}))
(def node (atom {}))

(defn init-node! [nid]
  (swap! goes-to assoc-in [nid 1] [])
  (swap! node assoc nid {:nid nid :version 1}))

(defn init-event! [[nid version]]
  (swap! goes-to assoc-in [nid version] [])
  (swap! node assoc-in [nid :version] version))

(defn happened-before! [src-event dest-event]
  (init-event! dest-event)
  (swap! goes-to update-in src-event conj dest-event))

(defn send! [src-nid dest-nid]
  (let [dest-version (:version (get @node dest-nid))
        src-event    [src-nid  (:version (get @node src-nid))]
        dest-event   [dest-nid dest-version]
        result-event [dest-nid (inc dest-version)]]
    (happened-before! src-event  result-event)
    (happened-before! dest-event result-event)))

(defn bump! [nid]
  (let [version    (:version (get @node nid))
        src-event  [nid version]
        dest-event [nid (inc version)]]
    (happened-before! src-event dest-event)))

(defn goes-to? [start-event end-event]
  (if (= end-event start-event)
    true
    (some #(goes-to? % end-event)
          (get-in @goes-to start-event))))


(def example
  {:a {1 [[:a 2]]
       2 [[:a 3]
          [:b 2]]
       3 []}
   :b {1 [[:b 2]]
       2 [[:b 3]]
       3 [[:c 3]]}
   :c {1 [[:c 2]]
       2 [[:c 3]]
       3 []}})

(defn verify []
  (reset! goes-to {})
  (reset! node {})
  (doseq [nid [:a :b :c]]
    (init-node! nid))
  (bump! :a)
  (send! :a :b)
  (bump! :c)
  (bump! :a)
  (bump! :b)
  (send! :b :c))

(goes-to? [:a 1] [:c 3])
