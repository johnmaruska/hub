(ns hub.database
  "Using multiple APIs together to get a music DB I actually care about.

  - Discogs provides: Genres, Styles, Labels"
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http]
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as string]
   [malli.core :as m]
   [malli.util :as mu]))

;;;; SQLite

(def db-spec {:classname   "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname     (io/resource "data/media.db")})

(defn insert-sqlite [table-name data]
  (jdbc/with-db-connection [conn db-spec]
    (jdbc/insert-multi! conn (keyword table-name) data)))

(def Release
  (m/schema
   [:map
    [:artist string?]
    [:album string?]
    [:title string?]
    [:year string?]
    [:country string?]]))

(def ReleaseLabel
  (m/schema
   [:map
    [:artist string?]
    [:album string?]
    [:label string?]]))

(def ReleaseStyle
  (m/schema
   [:map
    [:artist string?]
    [:album string?]
    [:style string?]]))

(def ReleaseGenre
  (m/schema
   [:map
    [:artist string?]
    [:album string?]
    [:genre string?]]))


;;;; CSV

(defn load-csv [csv-file]
  (with-open [reader (io/reader csv-file)]
    (let [[header & data] (csv/read-csv reader)]
      (vec (map #(zipmap (map keyword header) %) data)))))

(def CsvDvd
  (m/schema
   [:map
    [:Title string?]
    [:Type string?]
    [:Format string?]]))

(def CsvAlbum
  (m/schema
   [:map
    [:Artist string?]
    [:Album string?]
    [:Format string?]]))

;;;; Discogs

(defn discogs-authorization []
  (str "Discogs token=" (System/getenv "DISCOGS_TOKEN")))

(defn discogs-search [query-params]
  (-> "https://api.discogs.com/database/search"
      (http/get {:query-params query-params
                 :headers      {"Authorization" (discogs-authorization)}})
      (update :body #(json/parse-string % true))))

(defn matching-title? [[artist album] result]
  (= (string/lower-case (str artist " - " album))
     (string/lower-case (:title result))))

(defn find-album-masters [[artist-name album-name]]
  (let [response (discogs-search {:artist        artist-name
                                  :release_title album-name
                                  :type          "master"})]
    (filter (partial matching-title? [artist-name album-name])
            (get-in response [:body :results]))))

(defn get-master [master-id]
  (-> (client/get (str "https://api.discogs.com/masters/" master-id))
      :body (json/parse-string true)))

(defn find-recording-personnel [artist-name album-name track-name]
  (let [master-id       (:master_id (first (find-album-masters [artist-name album-name])))
        master-credits  (:credits (get-master master-id))]
    (filter #(= track-name (:title %)) master-credits)))

(comment
  (def masters
    (find-album-masters [artist-name album-name]))
  (def master
    (get-master (:master_id (first masters))))
  (keys master)


  )

(def DiscogsPagination
  (m/schema
   [:map
    [:per_page int?]
    [:pages int?]
    [:page int?]
    [:urls [:map [:last string?] [:next string?]]]
    [:items int?]]))

(def DiscogsSearchResult
  (mu/merge
   [:map
    [:type [:enum "release" "master" "artist"]]
    [:title string?]
    [:id int?]
    [:uri string?]
    [:thumb [:maybe string?]]
    [:resource_url string?]]
   (mu/optional-keys
    [:map
     [:format string?]
     [:label string?]
     [:catno string?]
     [:year int?]
     [:genre string?]
     [:style string?]
     [:country string?]])))

(def DiscogsSearchResponse
  (m/schema
   [:map
    [:pagination DiscogsPagination]
    [:results [:vector DiscogsSearchResult]]]))

;;;;; MusicBrainz

(def artist-name "Black Sabbath")
(def album-name "Never Say Die")

(def MusicBrainzArtistCredit
  (m/schema
   [:map
    [:name string?]
    [:artist [:map
              [:id string?]
              [:name string?]
              [:sort-name string?]
              [:disambiguation string?]]]]))

(def MusicBrainzRelease
  (m/schema
   [:map
    [:date string?]
    [:artist-credit [:vector MusicBrainzArtistCredit]]
    [:label-info [:map
                  [:catalog-number string?]
                  [:label [:map
                           [:id string?]
                           [:name string?]]]]]
    [:asin string?]
    [:title string?]
    [:track-count int?]
    [:id string?]
    [:country string?]]))

(defn yyyy-mm-dd? [s]
  (and s (= 3 (count (string/split s #"-")))))

(defn search-album [artist-name album-name]
  (-> (http/get (str "https://musicbrainz.org/ws/2/release/?query=artist:"
                     artist-name " AND album:" album-name "&fmt=json"))
      :body (json/parse-string true)
      :releases))

(defn get-album-info [album-id]
  (-> (client/get (str "https://musicbrainz.org/ws/2/release/" album-id "?inc=artists+recordings&fmt=json"))
      :body (json/parse-string true)))

(defn earliest-release [album-results]
  (->> album-results
       (filter #(yyyy-mm-dd? (:date %)))
       (sort-by :date)
       first))

(defn recording-ids [album-info]
  (->> album-info
       :media
       first
       :tracks
       (map :recording)
       (map #(get % :id))))

(defn get-recording-credits [recording-id]
  (let [recording-url (str "https://musicbrainz.org/ws/2/recording/" recording-id "?inc=artists+instrument&fmt=json")]
    (-> (client/get recording-url)
        :body
        (json/parse-string true))))

(defn get-recording-credit [recording-id]
  (let [recording-url (str "https://musicbrainz.org/ws/2/recording/" recording-id "?inc=artist-credits+releases&fmt=json")]
    (-> (client/get recording-url)
        :body
        (json/parse-string true))))

;;; Doesn't work.
(defn get-artist [artist-id]
  (let [artist-url (str "https://musicbrainz.org/ws/2/artist/" artist-id)]
    (-> (client/get artist-url
                    {:query-params {:inc "instruments"}
                     :headers {"User-Agent" "myapp/1.0.0"}})
        :body
        (json/parse-string true))))


;; Doesn't work.
(defn get-instruments [recording-credit]
  (-> recording-credit
      :artist-credit
      (map :artist)
      (map #(select-keys % [:name :id]))
      (map #(let [artist-info (-> % :id
                                  (str "https://musicbrainz.org/ws/2/artist/")
                                  (client/get {:query-params {:inc "instruments"}
                                               :headers {"User-Agent" "myapp/1.0.0"}})
                                  :body
                                  json/parse-string)]
              (assoc % :instruments (get-in artist-info [:instrument-list :instrument :name]))))))

;; Doesnt work
(defn get-artist-instruments [artist-id]
  (let [recordings-url (str "https://musicbrainz.org/ws/2/recording?query=arid:" artist-id "&fmt=json&inc=artist-credits")]
    (-> (client/get recordings-url)
        :body
        (json/parse-string true)
        :recordings
        first
        :artist-credit
        first
        )))



(comment

  :recordings
  (mapcat :artist-credit)
  (mapcat :name-credit)
  (filter #(= artist-id (:artist %)))
  (mapcat :instrument-list)
  (mapcat :instrument)
  (map :name)
  distinct
  )



(comment

  (def recordings
    (-> (search-album artist-name album-name)
        earliest-release
        :id
        get-album-info
        recording-ids))

  (def recording-credit
    (get-recording-credit (first recordings)))

  (def artist-id
    (-> recording-credit
        :artist-credit
        first :artist
        :id))
  )

(defn get-album-personnel [artist-name album-name]
  (let [album-id   (:id (earliest-release (search-album artist-name album-name)))
        album-info (get-album-info album-id)]
    (recording-ids album-info)))
