(ns hub.server.inventory
  (:require
   [hub.util.data-file :as data-file]
   [malli.util :as mu]))

(def albums-csv "inventory/albums.csv")

(def album-spec
  [:map
   [:artist string?]
   [:release string?]
   ;; TODO: one of Vinyl, Digital, CD
   [:ownership string?]])

(defn albums
  "Vector of all albums in my inventory."
  []
  (->> (data-file/load-csv albums-csv)
       (filter #(every? not-empty (vals %)))))

(defn results [entity]
  [:map [:results [:vector entity]]])

(defn get-albums []
  {:status  200
   :headers {"Access-Control-Allow-Origin"  "*"
             "Access-Control-Allow-Headers" "Content-Type"}
   :body    {:results (albums)}})

(def routes
  ["/inventory"
   ["/albums" {:get  {:handler    get-albums
                      :responses  {200 {:body (results (mu/optional-keys album-spec))}}
                      :parameters {:query (mu/optional-keys album-spec)}}}]])
