(ns hub.cli.id3-fix
  "Windows Media Player, when auto-ripping a CD, will often miss the track data
  for the first track (album, artist, etc. not track title or name though).

  Running should correct all instances of this within ROOT-DIR"
  (:require
   [claudio.id3 :as id3]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def HOME "c:\\Users\\jackm")
(def ROOT-DIR (io/file HOME "Music" "Ripped CDs"))

;; disable noisy verbose logger
(.setLevel (java.util.logging.Logger/getLogger "org.jaudiotagger")
           java.util.logging.Level/OFF)

;;; Clojure wrappers on java.io.File

(defn get-name   [file] (.getName file))
(defn directory? [file] (.isDirectory file))
(defn list-files [file] (.listFiles file))

;;; string helpers

(defn remove-mp3-extension [s]
  (first (str/split s #".mp3")))

(defn remove-leading-term [s]
  (->> (str/split s #" ")
       (drop 1)
       (str/join " ")))

;;; file helpers

(defn mp3? [file]
  (str/ends-with? (get-name file) ".mp3"))

(defn track-title [file]
  (-> (get-name file) remove-mp3-extension remove-leading-term))

;;; tags helpers

(defn all-present? [{:keys [album artist genre title] :as tags}]
  (not (or (str/starts-with? album "Unknown album")
           (= artist "Unknown artist")
           (= genre "Unknown genre")
           (re-matches #"Track \d+$" title))))

;;; directory helpers

(defn contained-directories [dir]
  (filter directory? (list-files dir)))

(defn mp3s [dir]
  (filter mp3? (list-files dir)))

;;; collection of directory-contained files helpers

(defn track-tags [files]
  (->> (map id3/read-tag files)
       (filter all-present?)
       first))

(defn enrich-tags [files]
  (-> (track-tags files)
      (assoc :track-total (str (count files)))
      (dissoc :track :title)))

(defn broken-tracks [files]
  (filter (comp not all-present? id3/read-tag) files))

;;; track helpers

(defn with-title [track]
  (assoc-in track [:tags :title] (track-title (:file track))))

(defn write-tags!
  "Write the given `tags` to given `file`."
  [{:keys [tags file]}]
  (println "Writing file" (get-name file) "with tags" tags)
  (apply id3/write-tag! file (flatten (vec tags))))

(defn correct-track! [track tags]
  (write-tags! (with-title {:file track :tags tags})))

;;; application specific fns

(defn disc-dirs
  "Get all directories associated with a disc assuming they're nested below an
  artist directory from the root."
  [root-dir]
  (->> (contained-directories root-dir)  ; artist directories
       (map contained-directories)       ; disc directories
       (apply concat)))

(defn correct-tracks!
  "Correct id3 tags for all tracks within given `dir`"
  [dir]
  (println "Correcting tracks for dir" (get-name dir))
  (let [mp3s (mp3s dir)
        tags (enrich-tags mp3s)]
    (run! #(correct-track! % tags) (broken-tracks mp3s))))

(defn apply-fix!
  "Correct id3 tags for all files with unknown tags, based on their neighboring
  track files.

  Applies across all directories within the root."
  []
  (run! correct-tracks! (disc-dirs ROOT-DIR)))
