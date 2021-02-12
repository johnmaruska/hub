(ns hub.sketchbook.logical-clocks.causal-history
  "Basic implementation of Figure 2 causal histories approach in ACM's
  Why Logical Clocks are Easy  https://queue.acm.org/detail.cfm?id=2917756"
  (:require [clojure.set :as set])
  (:refer-clojure :exclude [send]))

(defn init-node [state nid]
  (-> state
      (assoc-in [:history nid 1] #{[nid 1]})
      (assoc-in [:node nid] 1)))

(defn version [state nid]
  (get-in state [:node nid]))

(defn history [state nid]
  (get-in state [:history nid (version state nid)]))

(defn add-event [state [nid ver] history]
  (-> state
      (assoc-in [:history nid ver]
                (set/union history #{[nid ver]}))
      (assoc-in [:node nid] ver)))

;;;; interface

(defn send [state src-nid dest-nid]
  (let [event [dest-nid (inc (version state dest-nid))]]
    (add-event state event
               (set/union (history state dest-nid)
                          (history state src-nid)))))

(defn bump [state nid]
  (let [event [nid (inc (version state nid))]]
    (add-event state event (history state nid))))

(defn caused-by? [state end-event start-event]
  (contains? (get-in (:history state) end-event) start-event))
