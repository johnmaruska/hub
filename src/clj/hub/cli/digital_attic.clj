(ns hub.cli.digital-attic
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.tools.cli :refer [parse-opts]]))

;; ----- Configuration -------------------------------------------------

(def ^:dynamic *CONFIG*)

(defn read-config [config-file]
  (when (.exists (io/file config-file))
    (->> (string/split (slurp config-file) #"\n--\n")
         (map #(string/split % #"\n"))
         (reduce (fn [acc [category & entries]]
                   (cond
                     (= "Root:" category)
                     (assoc acc :root (vec entries))
                     (= "Ignored Folders:" category)
                     (assoc-in acc [:ignored :folders] (set entries))
                     (= "Ignored URLs:" category)
                     (assoc-in acc [:ignored :urls] (set entries))
                     :else acc)) {}))))

;; ----- Localize Remote Files -----------------------------------------

;; TODO download remote files to local disk

(def unlocalized-entry #"^- \[(.*)\]\((.*)\)$")

(defn markdown-link [title uri]
  (str "[" title "](" uri ")"))

(defn updated-entry [title local-path remote-uri]
  (str "- [" (markdown-link "Local" local-path)
       "][" (markdown-link "Remote" remote-uri)
       "] " title))

(defn localize! [input-markdown output-markdown storage-dir]
  (let [contents (with-open [reader (io/reader input-markdown)]
                   (vec (line-seq reader)))]
    (with-open [writer (io/writer output-markdown)]
      (doseq [line contents]
        (if-let [matches (re-find unlocalized-entry line)]
          (let [[title uri] (rest matches)]
            ;; download static files
            (.write writer (str (updated-entry title "LOCAL" uri) "\n")))
          (.write writer (str line "\n"))))))  )


;; ----- Convert JSON --------------------------------------------------

(defn entry->str [node]
  (format "- [%s](%s)\n" (:title node) (:uri node)))

(defn folder->str [node depth]
  (str (apply str (repeat depth "#")) " " (:title node) "\n"))

(defn entry? [node]
  (and (:uri node)
       (not-any? #(re-find (re-pattern %) (:uri node))
                 (-> *CONFIG* :ignored :urls))))

(defn folder? [node path]
  (and (:children node)
       (not-any? #(string/starts-with? (string/join "/" path) %)
                 (-> *CONFIG* :ignored :paths))))

(def PRE-ENTRIES-SPACE "\n")
(def PRE-HEAD-SPACE "\n\n")

(defn entries->string [node]
  (->> (:children node)
       (filter entry?)
       (map entry->str)
       (apply str)))

(declare folders->string)  ; circular recursion yay
(defn parse-folder
  "Parses main contents of a single folder. Header, entries, children."
  [node prev-path]
  (let [path (concat prev-path [(:title node)])
        space-children (fn [s] (str (if (seq s) PRE-HEAD-SPACE "") s))]
    (str (folder->str node (count path))
         PRE-ENTRIES-SPACE
         (entries->string node)
         (space-children (folders->string node path)))))

(defn folders->string
  "Converts the nested folders of a node to string by parsing each of them."
  ([node] (folders->string node []))
  ([node path]
   (->> (:children node)
        (filter #(folder? % path))
        (map #(parse-folder % path))
        (string/join PRE-HEAD-SPACE))))

(defn select-root [bookmarks root-path]
  (reduce (fn move-root [current-root nested-key]
            (first (filter #(= nested-key (:title %))
                           (:children current-root))))
          bookmarks
          root-path))

(defn bookmarks->markdown [bookmarks]
  (let [root (select-root bookmarks (:root *CONFIG*))]
    (str "# Digital Attic\n"
         "\n\n";; TODO: insert preamble
         "# Uncategorized\n"
         PRE-ENTRIES-SPACE (entries->string root)
         PRE-HEAD-SPACE (folders->string root))))

(defn import! [input-json output-markdown config-file]
  (binding [*CONFIG* (read-config config-file)]
    (let [bookmarks (json/read-str (slurp input-json) :key-fn keyword)]
      (->> (bookmarks->markdown bookmarks)
           (spit (str output-markdown ".raw.md"))))))

;; ----- Main! ---------------------------------------------------------

(def cli-options
  [["-i" "--input FILE" "File to be operated on"
    :validate [#(.exists (io/file %)) "Must be an existing file"]]
   ["-o" "--output FILE" "Generated markdown file"
    :default "attic.md"]
   ["-c" "--config FILE" "Configuration text file"]
   ["-d" "--dir DIR" "Storage directory for localized files"
    :default "storage/"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [arguments options errors]} (parse-opts *command-line-args* cli-options)
        {:keys [output input config dir]}  options]
    (if errors
      (println "ERROR -" errors)
      (case (first arguments)
        "import"   (import! input output config)
        "localize" (localize! input output dir)
        nil        (do (import! input output config)
                       (localize! output output dir))))))
