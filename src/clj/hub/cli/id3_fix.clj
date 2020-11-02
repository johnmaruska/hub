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

;; Clojure wrappers on java.io.File
(defn get-name   [file] (.getName file))
(defn directory? [file] (.isDirectory file))
(defn list-files [file] (.listFiles file))

(defn get-contained-directories [dir]
  (filter #(.isDirectory %) (list-files dir)))

(defn get-disc-dirs
  "Get all directories associated with a disc assuming they're nested below an
  artist directory from the root."
  [root-dir]
  (->> (get-contained-directories ROOT-DIR)  ; artist directories
       (map get-contained-directories)       ; disc directories
       flatten))

(defn mp3? [file] (str/ends-with? (get-name file) ".mp3"))

(defn get-mp3s [dir] (filter mp3? (list-files dir)))

(defn all-present? [{:keys [album artist genre title track-total] :as tags}]
  (not (or (str/starts-with? album "Unknown album")
           (= artist "Unknown artist")
           (= genre "Unknown genre")
           (re-matches #"Track \d+$" title))))

(defn get-first-known-track-tags [files]
  (->> (map id3/read-tag files)
       (filter all-present?)
       first))

(defn get-correct-tags [files]
  (-> (get-first-known-track-tags files)
      (assoc :track-total (str (count files)))
      (dissoc :track :title)))

(defn get-broken-tracks [files]
  (filter (comp not all-present? id3/read-tag) files))

(defn remove-mp3-extension [s] (first (str/split s #".mp3")))

(defn remove-leading-term [s]
  (->> (str/split s #" ")
       (drop 1)
       (str/join " ")))

(defn get-track-title [file]
  (-> (get-name file)
      remove-mp3-extension
      remove-leading-term))

(defn with-track-title [{:keys [file] :as track}]
  (assoc-in track [:tags :title] (get-track-title file)))

(defn write-tags!
  "Write the given `tags` to given `file`.
  Also updates `:title` tag. I couldn't find a better way to split that."
  [{:keys [tags file]}]
  (println "Writing file" (get-name file) "with tags" tags)
  (apply id3/write-tag! file (flatten (vec tags))))

(defn correct-tracks!
  "Correct id3 tags for all tracks within given `dir`"
  [dir]
  (let [mp3s          (get-mp3s dir)
        tags          (get-correct-tags mp3s)
        broken-tracks (get-broken-tracks mp3s)]
    (run! #(-> {:file % :tags tags}
               with-track-title
               write-tags!)
          broken-tracks)))

(defn apply-fix!
  "Correct id3 tags for all files with unknown tags, based on their neighboring
  track files.

  Applies across all directories within the root."
  []
  (run! correct-tracks! (get-disc-dirs ROOT-DIR)))
