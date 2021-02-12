(ns hub.sketchbook.logical-clocks.happened-before
  "Basic implementation of Figure 1 happened-before relation in ACM's
  Why Logical Clocks are Easy  https://queue.acm.org/detail.cfm?id=2917756

  This is useful pretty much exclusively for full-knowledge analysis, and not
  very efficient for that. Best used for illustration not computation."
  (:refer-clojure :exclude [send]))

(defn init-node [state nid]
  (-> state
      (assoc-in [:goes-to nid 1] [])
      (assoc-in [:node nid] {:nid nid :version 1})))

(defn happened-before [state src-event dest-event]
  (let [[src-nid src-ver] src-event
        [dest-nid dest-ver] dest-event]
    (-> state
        ;; bump destination node to destination event
        (assoc-in [:node dest-nid :version] dest-ver)
        ;; destination event does not yet go anywhere
        (assoc-in [:goes-to dest-nid dest-ver] [])
        ;; source event goes to destination event
        (update-in [:goes-to src-nid src-ver]
                   conj dest-event))))

;;;; interface

(defn send [state src-nid dest-nid]
  (let [dest-version (:version (get-in state [:node dest-nid]))
        src-event    [src-nid  (:version (get-in state [:node src-nid]))]
        dest-event   [dest-nid dest-version]
        result-event [dest-nid (inc dest-version)]]
    (-> state
        (happened-before src-event  result-event)
        (happened-before dest-event result-event))))

(defn bump [state nid]
  (let [version    (:version (get-in state [:node nid]))
        src-event  [nid version]
        dest-event [nid (inc version)]]
    (happened-before state src-event dest-event)))

(defn goes-to? [state start-event end-event]
  (if (= end-event start-event)
    true
    (some #(goes-to? state % end-event)
          (get-in (:goes-to state) start-event))))
