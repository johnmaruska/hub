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

(defn page-title [contents]
  (let [find-tag (fn [[_ _ & children] tag]
                   (first (filter #(= tag (first %)) children)))]
    (-> contents (find-tag :head) (find-tag :title) (nth 2))))

(defn uri-str? [s]
  (= 2 (count (string/split s #"://"))))

(defn image? [filename]
  (some #(string/ends-with? filename %) [".jpeg" ".jpg" ".png" ".svg"]))

(defn basename [filename] (string/replace filename #"/[^/]+\.[^/]+$" ""))
(defn extension [filename] (when (string/includes? filename ".")
                             (last (string/split filename #"\."))))

(defn byte-array? [x] (= (Class/forName "[B") (.getClass x)))

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

(defn paths [target {:keys [uri dir] :as config}]
  (if (uri-str? target)
    {:remote target
     :local  (str "./" (url-encode target))}
    {:remote (str (url (basename uri) target))
     :local  (str "./" (string/replace target #"\.\./" ""))}))

(defn localize-node! [tag uri-key node config]
  (assert (= tag (html/tag node)))
  (if (localizable? (uri-key (html/attributes node)))
    (let [{:keys [local remote]} (paths (uri-key (html/attributes node)) config)
          local-attrs (assoc (html/attributes node) uri-key local)]
      (download-resource! remote (string/replace-first local #"./" (:dir config)))
      (assoc node 1 local-attrs))
    node))

(def uri-key {:meta :content, :link :href, :img :src})

(defn localize! [node config]
  (let [tag (and (vector? node) (first node))]
    (if-let [k (get uri-key tag)]
      (localize-node! tag k node config)
      node)))

;; ----- Main ----------------------------------------------------------

(let [uri            "https://100r.co/site/home.html"
      contents       (html/parse uri)
      dir            (str "/Users/maruska/digital_attic/storage/"
                          (url-encode (page-title contents)))
      local-contents (w/postwalk #(localize! % {:uri uri :dir dir}) contents)]
  (write (str dir "/index.html")
         (hiccup/html local-contents)))
