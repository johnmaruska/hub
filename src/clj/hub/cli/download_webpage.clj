(ns hub.cli.download-webpage
  (:require
   [clojure.walk :as w]
   [clj-http.client :as http]
   [cemerick.url :refer [url-encode url]]
   [hiccup.core :as hiccup]
   [pl.danieljanus.tagsoup :as html]
   [clojure.string :as string]
   [clojure.java.io :as io]))

;; ----- Helpers -------------------------------------------------------

(defn page-title
  "Extract page title from .head.title selector."
  [contents]
  (let [find-tag (fn [[_ _ & children] tag]
                   (first (filter #(= tag (first %)) children)))]
    (try
      (-> contents (find-tag :head) (find-tag :title) (nth 2))
      (catch IndexOutOfBoundsException _
        "PAGETITLE"))))

(defn uri-str? [s]
  (= 2 (count (string/split s #"://"))))

(defn website-name [uri]
  (-> uri
      (string/split #"://") last
      (string/split #"/") first
      (string/replace-first #"www." "")))

(defn image? [filename]
  (some #(string/ends-with? filename %)
        [".jpeg" ".jpg" ".png" ".svg"]))

;; TODO: is there a clearer way to do this than regex?
(defn parent-dir [filename]
  (string/replace filename #"/[^/]+\.[^/]+$" ""))

(defn basename [filename]
  (last (string/split filename #"/")))

(defn extension [filename]
  (when (string/includes? filename ".")
    (last (string/split filename #"\."))))

(defn byte-array? [x]
  (= (Class/forName "[B") (.getClass x)))

;; ----- Input/Output --------------------------------------------------

(defn fetch [uri & [opts]]
  (let [resp (http/get uri opts)]
    (if (= 200 (:status resp))
      (:body resp)
      (throw (ex-info "Got non-200 status"
                      {:type     :failed-network-request
                       :uri      uri
                       :response resp})))))

(defn write [filename contents]
  (io/make-parents filename)
  (let [writer (if (byte-array? contents)
                 io/output-stream #(io/writer (io/file %)))]
    (with-open [w (writer filename)]
      (.write w contents))))

(defn download-resource! [source target]
  (try
    (let [opts (if (image? source)
                 {:as :byte-array}
                 {})]
      (write target (fetch source opts)))
    (catch clojure.lang.ExceptionInfo ex
      (println "ERROR" (ex-message ex) "from" source))))

;; ----- Localization --------------------------------------------------

(defn localizable? [s]
  (and s (or (image? s) (string/ends-with? s ".css"))))

(defn paths [target {:keys [uri]}]
  (if (uri-str? target)
    {:remote target
     :local  (str "./" (url-encode target))}
    {:remote (str (url (parent-dir uri) target))
     :local  (str "./" (string/replace target #"\.\./" ""))}))

(defn localize-node! [tag uri-key node config]
  (assert (= tag (html/tag node)))
  (if (localizable? (uri-key (html/attributes node)))
    (let [{:keys [local remote]} (paths (uri-key (html/attributes node))
                                        config)
          local-attrs (assoc (html/attributes node) uri-key local)
          target-file (string/replace-first local #"./"
                                            (str (:dir config) "/"))]
      (download-resource! remote target-file)
      (assoc node 1 local-attrs))
    node))

(def uri-key {:meta :content, :link :href, :img :src})

(defn localize! [node config]
  (let [tag (and (vector? node) (first node))]
    (if-let [k (get uri-key tag)]
      (localize-node! tag k node config)
      node)))

;; ----- Main ----------------------------------------------------------

(defn download-pdf!
  "Download a PDF at `uri` to directory `storage-dir`.

  Creates a directory for the base website, and maintains pdf name."
  [uri storage-dir]
  (let [outfile (->> [storage-dir (website-name uri) (basename uri)]
                     (string/join "/"))]
    (write outfile uri)
    outfile))

#_
(page-title (html/parse "https://tobyrush.com/theorypages/index.html"))

(defn download-webpage!
  "Download contents of `uri` to directory `storage-dir`.

  Creates a directory for the base website, and a file for that page title.
  Tracks `failed-files` atom for handling any issues."
  [uri storage-dir failed-files]
  (try
    (let [contents (html/parse uri)
          outdir   (->> [storage-dir
                         (website-name uri)
                         (url-encode (page-title contents))]
                        (string/join "/"))
          outfile  (str outdir "/index.html")]
      (->> contents
           (w/walk #(localize! % {:uri uri :dir outdir})
                   identity)
           hiccup/html
           (write outfile))
      outfile)
    (catch Exception ex
      (swap! failed-files conj {:uri       uri
                                :exception ex})
      (println "DOWNLOAD ERROR - " (ex-message ex)))))
