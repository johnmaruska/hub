(ns hub.cli.download-webpage
  (:require
   [clojure.walk :as w]
   [clj-http.client :as http]
   [cemerick.url :refer [url-encode url]]
   [hiccup.core :as hiccup]
   [pl.danieljanus.tagsoup :as html]
   [clojure.string :as string]
   [clojure.java.io :as io])
  (:import
   (java.io ByteArrayInputStream)
   (java.net URL)
   (javax.imageio ImageIO)))

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
      (try
        (.write w contents)
        (catch java.io.FileNotFoundException ex
          (print "writer" writer "filename" filename))
        (catch IllegalArgumentException ex
          (print (type (type contents))))))))

(defn download-resource! [source target]
  (println "download resource from" source "to" target)
  (try
    (let [opts (if (image? source)
                 {:as :byte-array}
                 {})]
      (println "opts" opts "source" source "target" target)
      (write target (fetch source opts)))
    (catch clojure.lang.ExceptionInfo ex
      (println "ERROR" (ex-message ex) "from" source))))


;; ----- Localization --------------------------------------------------

(def always-localized-extensions [".css" ".html"])
(def image-extensions [".jpeg" ".jpg" ".png"])

(defn localizable? [s]
  (and s (or (image? s) (some #(string/ends-with? s %)
                              always-localized-extensions))))

(defn paths [target {:keys [uri dir] :as config}]
  (if (uri-str? target)
    {:remote target
     :local  (str "./" (url-encode target))}
    {:remote (str (url (basename uri) target))
     :local  (str "./" (string/replace target #"\.\./" ""))}))

(defn localize-node! [tag uri-key [node-tag attributes & children] config]
  (assert (= tag node-tag))
  (if (localizable? (uri-key attributes))
    (let [{:keys [local remote]} (paths (uri-key attributes) config)
          local-attrs (assoc attributes uri-key local)]
      (download-resource! remote local)
      (vec (concat [node-tag local-attrs] children)))
    (vec (concat [node-tag attributes] children))))

(def uri-key {:meta :content
              :link :href
              :img :src})

(defn localize! [node config]
  (let [tag (and (vector? node) (first node))
        f   (if-let [k (get uri-key tag)]
              (partial localize-node! tag k)
              (fn node-identity [a b] a))]
    (f node config)))

;; ----- Main ----------------------------------------------------------

(let [uri "https://100r.co/site/home.html"
      dir "/Users/maruska/digital_attic/storage"

      contents (html/parse uri)
      title    (page-title contents)
      config   {:uri uri
                :dir (str dir "/" (url-encode title))}

      local-contents (w/postwalk #(localize! % config) contents)]
  (write (str (:dir config) "/index.html")
         (hiccup/html local-contents)))
