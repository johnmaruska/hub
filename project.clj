(defproject hub "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.csv "1.0.0"]
                 ;; work with mp3 id3 tags
                 ;; https://github.com/pandeiro/claudio
                 [claudio "0.1.3"]
                 ;; discord api wrapper
                 [discord.clj "2.0.0"]]
  :source-paths ["src/clj"])
