(ns hub.core)

(enable-console-print!)
(println "This text is printed from src/cljs/hub/core.cljs.")

;; don't overwrite app-state on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; can force rerendering here by modifying app-state
  )

(js/console.log "Hello, Hub!")
