(ns hub.keyforge.event-loop)

;;;; ---- Queue --------------------------------------------------------

(defn queue
  ([] (clojure.lang.PersistentQueue/EMPTY))
  ([coll]
   (reduce conj clojure.lang.PersistentQueue/EMPTY coll)))

;;;; ---- Event Loop ---------------------------------------------------

(def events-register (atom {}))
(defmacro defevent [sy args & body]
  `(do (defn ~sy ~args
         (let [result# (do ~@body)]
           (if (contains? result# ::state)
             result#
             {::state result#})))
       (swap! events-register
              assoc (keyword (str *ns*) (str '~sy))
              (var ~sy))))

(def events-queue (atom (queue)))
(defn ->event-vector [event]
  (if (keyword? event) [event] event))
(defn dispatch-event! [event]
  (swap! events-queue conj (->event-vector event)))

(def default-events-sequence (atom {}))
(defn set-default-event-sequence! [m]
  (reset! default-events-sequence m))

(defn handle-event!* [state {:keys [handler args] :as event}]
  (println "Handling event" (:name event) args handler)
  (try
    (apply handler state args)
    (catch clojure.lang.ArityException ex
      (throw (ex-info "Exception when applying event handler." event ex)))))

(defn handle-event! [state [event-name & args]]
  (let [handler    (or (get @events-register event-name)
                       identity)
        result     (handle-event!* state {:name    event-name
                                          :handler handler
                                          :args    args})
        next-event (or (::dispatch result)
                       (get @default-events-sequence event-name))]
    (when next-event
      (dispatch-event! next-event))
    (or (::state result) result)))

;; TODO: this will stop when queue is empty. Use channels to do infinitely
(defn process-queue! []
  (loop [state {}]
    (if-let [event (peek @events-queue)]
      (do (swap! events-queue pop)
          (recur (handle-event! state event)))
      state)))
