(defproject hub "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.csv "1.0.0"]
                 ;; work with mp3 id3 tags
                 ;; https://github.com/pandeiro/claudio
                 [claudio "0.1.3"]
                 ;; discord api wrapper
                 [org.suskalo/discljord "1.1.1"
                  :exclusions [org.eclipse.jetty.websocket/websocket-api
                               org.eclipse.jetty.websocket/websocket-common
                               org.eclipse.jetty/jetty-http]]
                 ;; data-driven schemas that aren't clojure.spec
                 [metosin/malli "0.2.1"]
                 ;; http format negotiation
                 [metosin/muuntaja "0.6.7"]
                 ;; data-driven router
                 [metosin/reitit "0.5.10"]
                 ;; drawing library for Conway
                 [quil "3.1.0"]
                 ;; Ring web-server
                 [ring "1.8.2"]]

  :resource-paths ["target" "resources"]
  :source-paths   ["src/clj" "src/cljs" "src/cljc"]
  :test-paths     ["test/clj" "test/cljs" "test/cljc"]

  :profiles {:dev {:dependencies  [[binaryage/devtools "1.0.2"]
                                   [com.bhauman/figwheel-main "0.2.12"
                                    :exclusions [args4j]]
                                   [com.bhauman/rebel-readline-cljs "0.1.4"
                                    :exclusions [args4j]]
                                   [org.clojure/clojurescript "1.10.773"
                                    :exclusions [com.google.code.findbugs/jsr305]]
                                   [cljs-ajax "0.8.1"]
                                   [day8.re-frame/http-fx "0.2.1"]
                                   [reagent "1.0.0-alpha2"]
                                   [re-frame "1.1.2"]]
                   :plugins       [[lein-ancient "0.6.15"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths  ["dev"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}}

  :aliases {"cljs-repl"     ["trampoline" "run" "-m" "figwheel.main"]
            "cljs-dev-repl" ["trampoline" "run" "-m" "figwheel.main" "--build" "dev" "--repl"]})
