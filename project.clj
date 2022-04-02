(defproject hub "0.1.0-SNAPSHOT"
  :description "Playground Clojure app for all the things I want but am too lazy to make a dedicated project for."
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/data.xml "0.0.8"]  ; required for tagsoup
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 ;; work with mp3 id3 tags
                 [claudio "0.1.3"]  ; https://github.com/pandeiro/claudio
                 ;; URL encoding
                 [com.cemerick/url "0.1.1"]
                 [clj-http "3.12.1"]
                 ;; HTML/XML parsing
                 [clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
                 ;; discord api wrapper
                 ;; https://github.com/discljord/discljord
                 [com.github.discljord/discljord "1.3.1"]
                 ;; environ is in the Heroku example. Do we need it?
                 [environ "1.1.0"]
                 ;; HTML rendering
                 [hiccup "1.0.5"]
                 ;; context-free-grammar parsing
                 [instaparse "1.4.10"]
                 ;; retry management
                 [listora/again "1.0.0"]
                 ;; Component lifecycle management
                 [mount "0.1.16"]
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
  :source-paths   ["src/clj" "src/cljs" "src/cljc"]
  :test-paths     ["test/clj" "test/cljs" "test/cljc"]

  :profiles {:profiles {:production {:env {:production true}}}
             :dev {:dependencies  [[binaryage/devtools "1.0.2"]
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
                   :plugins       [[lein-ancient "0.6.15"]
                                   [venantius/yagni "0.1.7"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths  ["dev"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}}

  :main hub.core
  :aot [hub.core]
  :uberjar-name "hub.jar"
  :min-lein-version "2.0.0"
  :hooks [environ.leiningen.hooks]
  :plugins [[environ/environ.lein "0.3.1"]]

  :aliases {"cljs-repl"     ["trampoline" "run" "-m" "figwheel.main"]
            "cljs-dev-repl" ["trampoline" "run" "-m" "figwheel.main" "--build" "dev" "--repl"]})
