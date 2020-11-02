(ns hub.core
  (:gen-class))

(defn -main [& args]
  (println "hub core")
  (doseq [arg *command-line-args*]
    (println "arg" arg)))
