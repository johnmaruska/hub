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
           result#))
       (swap! events-register
              assoc (keyword (str *ns*) (str '~sy))
              (var ~sy))))

(def default-event-queue (atom (queue)))

(defn ->event-vector [event]
  (if (keyword? event) [event] event))
(defn dispatch-event!
  ([event] (dispatch-event! default-event-queue event))
  ([event-queue event]
   (swap! event-queue conj (->event-vector event))))

(def default-events-sequence (atom {}))

(defn handle-event! [event-queue state [event-name & args]]
  (let [handler    (or (get @events-register event-name)
                       (do
                         (println "Event-name" event-name "has no handler -- using identity")
                         identity))
        result     (do (println "Handling event" event-name args handler)
                       (apply handler state args))
        next-event (or (::dispatch result)
                       (get @default-events-sequence event-name))]
    (when next-event
      (dispatch-event! event-queue next-event))
    (or (::state result) result)))

;; TODO: this will stop when queue is empty. Use channels to do infinitely
(defn process-queue! []
  (loop [state {}]
    (if-let [event (peek @default-event-queue)]
      (do (swap! default-event-queue pop)
          (recur (handle-event! default-event-queue state event)))
      state)))
