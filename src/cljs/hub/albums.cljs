(ns hub.albums
  (:require
   [ajax.edn :as ajax]
   [clojure.string :as str]
   [re-frame.core :as rf]))

(rf/reg-event-fx
 ::fetch
 (fn [_ [_ query-params]]
   {:http-xhrio {:uri     "http://localhost:4000/inventory/albums"
                 :params  (or query-params {})
                 :method  :get
                 :timeout 10000
                 :on-success [::fetch-success]
                 :on-failure [::fetch-failure]
                 :format          (ajax/edn-request-format)
                 :response-format (ajax/edn-response-format)}}))

(rf/reg-event-fx
 ::fetch-success
 (fn [{db :db} [_ resp]]
   {:db (assoc-in db [:inventory :albums] resp)}))

(rf/reg-event-fx
 ::fetch-failure
 (fn [{db :db} [_ resp]]
   {:db (assoc-in db [:inventory :albums :error] resp)}))

(rf/reg-sub
 ::albums
 (fn [db _]
   (get-in db [:inventory :albums])))

(defn success-view [albums]
  [:table
   [:thead [:tr
            [:th "Artist"]
            [:th "Release"]
            [:th "Format"]]]
   [:tbody
    (doall
     (for [album (sort-by :artist albums)]
       ^{:key (str/join "+" (vals album))}
       [:tr
        [:td (:artist album)]
        [:td (:release album)]
        [:td (:ownership album)]]))]])

(defn failure-view [error]
  (println error)
  [:div "Got an error when fetching albums."])

(defn view []
  (let [albums (rf/subscribe [::albums])]
    (fn []
      (if (:results @albums)
        [success-view (:results @albums)]
        [failure-view (:error @albums)]))))
