(ns hub.500lines.graph-db.graph)

(defn error [& args]
  (apply println "Error:"))

(defn v [graph])
(defn add-vertex [graph vertex])
(defn add-edge [graph edge])
(defn add-vertices [graph vertices])
(defn add-edges [graph edges])

(defn find-vertices [graph args]
  (cond
    (map? (first args)) (search-vertices graph (first args))
    (empty? args) (:vertices @graph)
    :else (find-vertices-by-ids graph args)))

(defn find-vertices-by-ids [graph ids]
  (if (= 1 (count ids))
    (if-let [maybe-vertex (find-vertex-by-id (first ids))]
      [maybe-vertex] [])
    (->> ids
         (map (partial find-vertex-by-id graph))
         (filter boolean))))

(defn find-vertex-by-id [graph vertex-id]
  (get-in @graph [:vertexIndex vertex-id]))

(defn search-vertices [graph filter-f]
  (filter (fn [vertex]
            (object-filter vertex filter-f))
          (:vertices @graph)))

(defn find-out-edges [graph vertex])
(defn find-in-edges [graph vertex])

;;;;;;;;


(defn run [query])
(defn add [query pipetype args])

;;;;;;;


(defn make-gremlin [vertex state]
  (atom {:vertex vertex :state (or state {})}))

(defn go-to-vertex [gremlin vertex]
  (make-gremlin vertex (:state @gremlin)))

(defn in-sequence? [x xs]
  (some #{x} xs))

(defn object-filter [thing filter]
  (every? (fn [[k v]] (= (get thing k) v)) filter))

(defn filter-edges [filter]
  (fn [edge]
    (cond
      (not filter) true
      (string? filter) (= (:_label edge) filter)
      (sequential? filter) (in-sequence? edge filter)
      :else (object-filter edge filter))))


;;;; =========
;;;; Pipetypes
;;;; =========


;;; what in gods name are Pipetypes anyway?
;;; generally they take a gremlin and produce gremlins
;;; in vertex case, take a string and make a gremlin
;;;
;;; So we start on a vertex, make a gremlin, that gremlin moves along
;;; an edge, creating more gremlins and spreading on
;;; conditions. eventually we hit an end point and go from gremlin to
;;; string
;;;
;;; "pull" gets a gremlin from predecessor

;;; what state is the state argument to a pipetype referring to?
;;; gremlins maintain there own state, it must be something else?


;;; SO roughly what I understand to be happening I still don't know
;;; how we step through each part of this but, we start by making
;;; a "gremlin" on a vertex. a "gremlin" is just a vertex id and some
;;; state. Each pipe takes in the graph we're calling on, args to the
;;; command which created it, some global state, and a gremlin which
;;; has its own state. A pipe can mutate the state and propagate or
;;; wither gremlins. Filtering is done by returning "pull", stating
;;; you have no gremlin, or by returning the gremlin stating it passed
;;; the filter. Movement happens by cloning the gremlin and placing
;;; them on new vertices.

(defn faux-pipetype [_ _ maybe-gremlin _]
  (or maybe-gremlin "pull"))

;; addPipetype
(defn vertex-pipetype [graph args gremlin state]
  (when-not (:vertices @state)
    (swap! state assoc :vertices (find-vertices graph args)))
  (if (empty? (:vertices @state))
    "done"
    (do
      (let [vertex (last (:vertices @state))]
        (swap! state update :vertices butlast)
        (make-gremlin vertex (:state gremlin))))))

(defn simple-traversal [dir]
  (let [find-method (if (= dir "out") find-out-edges find-in-edges)
        edge-list   (if (= dir "out") :_in :_out)]
    (fn [_ args gremlin state]
      (letfn [(traverse-edge []
                (if (empty? (:edges @state))
                  "pull"
                  (let [vertex (edge-list (last (:edges @state)))]
                    (swap! state update :edges butlast)
                    (go-to-vertex (:gremlin @state) vertex))))
              (initialize-state []
                ;; work with one gremlin at a time
                (swap! state assoc :gremlin gremlin)
                ;; restrict to matching edges only
                (swap! state assoc :edges
                       (filter (filter-edges (first args))
                               (find-method (:vertex @gremlin)))))]
        (if (and (not gremlin) (empty? (:edges @state)))
          "pull"
          (do (when (empty? (:edges @state))
                (initialize-state))
              (if (empty? (:edges @state))
                "pull"
                (traverse-edge))))))))

(defn property-pipetype [_ args gremlin _]
  (if-not gremlin
    "pull"  ; query initialize
    (do
      (swap! gremlin assoc :result (get (:vertex @gremlin) (first args)))
      (if (nil? (:result @gremlin)) false gremlin))))

;; state is behaving like a cache, map of vertex IDs to visited? bool
(defn unique-pipetype [_ _ gremlin state]
  (cond
    (not gremlin) "pull"  ; query initialize
    (get @state (:_id @(:vertex @gremlin))) "pull"  ; reject repeats
    :else (do
            (swap! state assoc (:_id @(:vertex @gremlin)) true)
            gremlin)))

(defn filter-pipetype [_ args gremlin _]
  (cond
    (not gremlin) "pull"  ; query initialize
    ;; object filter applies
    (map? (first args)) (if (object-filter (:vertex @gremlin) (first args))
                          gremlin "pull")

    (not (fn? (first args)))
    (do (error (str "Filter is not a function: " (first args)))
        gremlin)  ; move along
    ;; fails filter
    (not ((first args) (:vertex @gremlin) gremlin)) "pull"
    ;; passes filter
    :else gremlin))

(defn take-pipetype [_ args gremlin state]
  (swap! state update :taken (fn [x] (or x 0)))  ; state initialize
  (cond
    (= (first args) (:taken @state))
    (do (swap! state assoc :taken 0)
        "done")

    (not gremlin)
    "pull"

    :else
    (do (update state :taken inc)
        gremlin)))

(defn as-pipetype [_ args gremlin _]
  (if (not gremlin)
    "pull"
    (swap! gremlin update-in [:state :as]
           ;; set the label to vertex
           assoc (first args) (:vertex @gremlin))))

(defn merge-pipetype [graph args gremlin state]
  ;; state init
  (when (and gremlin (empty? (:vertices @state)))
    (let [obj (or (-> @gremlin :state :as) {})]
      (->> args
           (map (fn [i] obj i))
           (filter boolean)
           (swap! state assoc :vertices))))
  (cond
    (and (not (:vertices @state)) (not gremlin))  ; query init
    "pull"

    (empty? (:vertices @state)) ; batch done
    "pull"

    :else
    (let [vertex (last (:vertices @state))]
      (swap! state update :edges butlast)
      (make-gremlin vertex (:state @gremlin)))))

(defn except-pipetype [graph args gremlin state]
  (cond
    (not gremlin)
    "pull"

    (= (:vertex @gremlin) (get-in @gremlin [:state :as (first args)]))
    "pull"

    :else
    gremlin))

(defn back-pipetype [graph args gremlin state]
  (if (not gremlin)
    "pull"
    (go-to-vertex gremlin (get-in @gremlin [:state :as (first args)]))))


(def pipetypes
  {"vertex"   vertex-pipetype
   "in"       (simple-traversal "in")
   "out"      (simple-traversal "out")
   "property" property-pipetype
   "unique"   unique-pipetype
   "filter"   filter-pipetype
   "take"     take-pipetype
   "as"       as-pipetype
   "back"     back-pipetype
   "except"   except-pipetype
   "merge"    merge-pipetype})

(defn get-pipetype [name]
  (or (get pipetypes name) faux-pipetype))

;;;; Interpreter time

;; read the step, eval pipetype function
;; input: (entire graph, own args, maybe gremlin, own local state)
;; output: one of [gremlin, false, "pull", "done"]
;; interpreter's state is "steps that are done" and "results"

(def MAX 100)


(defn try-previous-pipe [{:keys [pc] :as acc}]
  (merge acc {:pc (dec pc) :maybe-gremlin false}))

(defn previous-pipe-finished [{:keys [pc] :as acc}]
  (merge acc {:done pc :maybe-gremlin false}))

(defn pipe-done [{:keys [maybe-gremlin pc] :as acc}]
  (assoc acc :maybe-gremlin false :done pc))

(defn pipe-popped [{:keys [results done maybe-gremlin pc]} max]
  {:results       (if maybe-gremlin
                    (conj results maybe-gremlin)
                    results)
   :done          done
   :maybe-gremlin false
   :pc            (dec pc)})


(defn move-page [acc max]
  (let [acc     (update acc :pc inc)
        gremlin (:maybe-gremlin acc)
        results (:results acc)
        pc      (:pc acc)]  ;; next page
    (if (< max pc)  ; too far
      (merge {:results       (if gremlin (conj results gremlin) results)
              :maybe-gremlin false
              :pc            (dec pc)})
      acc)))

(defn show-properties [results]
  (map (fn [gremlin]
         (or (:result @gremlin) (:vertex @gremlin)))
       results))

(defn run [query]
  (let [;; Transform
        max (dec (count (:program @query)))]
    (loop [acc {:results       []
                :done          -1
                :maybe-gremlin false
                :pc            MAX}]
      (let [step          (get-in @query [:program (:pc acc)])
            ;; This probably needs to be an atom
            state         (get-in @query [:state (:pc acc)] {})
            pipetype      (get-pipetype (first step))
            maybe-gremlin (pipetype (:graph @query) (nth step 1)
                                    (:maybe-gremlin acc) state)]
        (cond
          (< max (:done acc)) (:results acc)

          (and (= "pull" maybe-gremlin)
               (> (dec (:pc acc)) (:done acc)))
          (recur (try-previous-pipe acc))

          :else
          (recur (cond-> acc
                   (= "pull" maybe-gremlin) (previous-pipe-finished)
                   (= "done" maybe-gremlin) (pipe-done)
                   true (move-page max))))))))
