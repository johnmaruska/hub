(defproject hub "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.csv "1.0.0"]
                 ;; work with mp3 id3 tags
                 ;; https://github.com/pandeiro/claudio
                 [claudio "0.1.3"]
                 ;; discord api wrapper
                 [org.suskalo/discljord "1.1.1"]
                 ;; data-driven schemas that aren't clojure.spec
                 [metosin/malli "0.2.1"]
                 ;; drawing library for Conway
                 [quil "3.1.0"]]

  :resource-paths ["target" "resources"]
  :source-paths   ["src/clj" "src/cljs"]
  :test-paths     ["test/clj" "src/cljs"]

  :profiles {:dev {:dependencies  [[binaryage/devtools "1.0.0"]
                                   [com.bhauman/figwheel-main "0.2.3"]
                                   [com.bhauman/rebel-readline-cljs "0.1.4"]
                                   [org.clojure/clojurescript "1.10.773"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths  ["src/cljs" "src/clj" "dev"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}}

  :aliases {"cljs-repl"     ["trampoline" "run" "-m" "figwheel.main"]
            "cljs-dev-repl" ["cljs-repl" "--" "--build" "dev" "--repl"]})
