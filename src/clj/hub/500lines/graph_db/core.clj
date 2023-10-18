(ns hub.500lines.graph-db.core
  "https://aosabook.org/en/500L/dagoba-an-in-memory-graph-database.html"
  (:refer-clojure :exclude [parents]))

;;;; Take One

(def V [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15])
(def E [[1 2] [1 3] [2 4] [2 5] [3 6] [3 7]
        [4 8] [4 9] [5 10] [5 11] [6 12] [6 13] [7 14] [7 15]])


(defn val-in-vec? [x xs]
  (some? (some #{x} xs)))

(defn parent [edge] (first edge))
(defn child [edge] (second edge))

(defn has-parent? [vertices [parent child]]
  (val-in-vec? parent vertices))
(defn has-child? [vertices [parent child]]
  (val-in-vec? child vertices))

(defn parents [vertices]
  (->> E (filter (partial has-child? vertices)) (map parent)))

(defn children [vertices]
  (->> E (filter (partial has-parent? vertices)) (map child)))

;;;; Build a Better Graph

(defrecord Vertex [id in out])
(defn mk-vertex []
  (Vertex. (random-uuid) [] []))

(defrecord Edge [id in out])
(defn mk-edge [^Vertex in ^Vertex out]
  (Edge. (random-uuid) (:id in) (:id out)))


;; I'm choosing to change the representation from [edge], [vertex],
;; {vertexId vertex} to just [edge] {vertexId vertex}.
(defrecord Graph [edges vertices])
(defn mk-graph []
  (Graph. {} {}))

(defn get-vertex [graph vertex-id]
  (get (:vertices graph) vertex-id))

(defn add-vertex [graph vertex]
  (when (get-vertex graph (:id vertex))
    (throw (ex-info "A vertex with that ID already exists"
                    {:error :Dagoba/AddVertexError
                     :vertex vertex})))
  (update graph :vertices assoc (:id vertex) vertex))

(defn add-edge [graph edge]
  (let [in-vertex  (get-vertex graph (:in edge))
        out-vertex (get-vertex graph (:out edge))]
    (when-not (and in-vertex out-vertex)
      (throw (ex-info "Missing a vertex for this edge."
                      {:edge       edge
                       :in-vertex  in-vertex
                       :out-vertex out-vertex})))
    (-> graph
        (update-in [:vertices (:in edge) :in] conj (:id edge))
        (update-in [:vertices (:out edge) :out] conj (:id edge))
        (update :edges assoc (:id edge) edge))))

(defn add-vertices [graph vertices]
  (reduce add-vertex graph vertices))

(defn add-edges [graph edges]
  (reduce add-edge graph edges))

;;;; Enter the Query

;;; The writeup here HEAVILY leverages JavaScript's prototype
;;; capabilities. Converting them to Clojure is the whole challenge
;;; but oof ouchie my brain. A direct mapping is possible but very
;;; un-Clojure and I don't want to go for that yet.

(defrecord Query [graph state program gremlins])
(defn mk-query [graph] (Query. graph [] [] []))

(defn add-step [query pipetype args]
  (let [step [pipetype args]]
    (update query :program conj step)))

(defn v [graph & args]
  (add-step (mk-query graph) "vertex" args))

(def v1 (mk-vertex))
(def v2 (mk-vertex))
(def e1 (mk-edge v1 v2))
(def graph
  (-> (mk-graph)
      (add-vertices [v1 v2])
      (add-edges [e1])))
(v graph "Thor")

;;;; Pipetypes

;;; I think I need to just read up on pipetype and gremlins.

;; TODO: how do i deal with the mutated Q namespace
(defn add-pipetype [pipetypes name f]
  [#_ P (assoc pipetypes name f)
   #_ Q (assoc Q name (fn [query & args] (add-step query name args)))])

(defn faux-pipetype [_ _ maybe-gremlin]
  (or maybe-gremlin || "pull"))

(defn get-pipetype [pipetypes name]
  (or (get pipetypes name) faux-pipetype))


(add-pipetype pipetypes "vertex"
              (fn [graph args gremlin state]
                ;; TODO: I am here
                ))
