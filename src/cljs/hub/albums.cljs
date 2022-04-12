(ns hub.albums
  (:require
   [ajax.edn :as ajax]
   [clojure.string :as string]
   [hub.fuzzy-search :refer [fuzzy-search]]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(def BACKEND_URL "http://localhost:4000/inventory/albums")
(def FORMATS ["CD" "Vinyl" "DVD" "Blu-ray" "Digital"])

(defn dropdown [params options]
  (letfn [(->option [opt-name]
            [:option {:value opt-name :key opt-name} opt-name])]
    [:select params (map ->option options)]))

(defn text-input [opts]
  [:input (merge {:type "text"} opts)])

(defn success-view [albums]
  (let [artist-filter (r/atom "")
        album-filter  (r/atom "")
        format-filter (r/atom "")
        update!       #(reset! %1 (.. %2 -target -value))]
    (fn []
      (let [shown-albums (-> albums
                             (fuzzy-search @artist-filter :key-fn :artist)
                             (fuzzy-search @album-filter :key-fn :release)
                             (fuzzy-search @format-filter :key-fn :ownership))]
        [:table {:class "inventory"}
         [:thead
          [:tr
           [:th "Artist" [text-input {:id "album--artist"
                                      :on-change #(update! artist-filter %)}]]
           [:th "Release" [text-input {:id "album--name"
                                       :on-change #(update! album-filter %)}]]
           [:th "Format" [dropdown {:id "album--format"
                                    :on-change #(update! format-filter %)}
                          (concat [""] FORMATS)]]]]
         [:tbody
          (doall
           (for [album (sort-by :artist shown-albums)]
             ^{:key (string/join "+" (vals album))}
             [:tr
              [:td (:artist album)]
              [:td (:release album)]
              [:td (:ownership album)]]))]]))))

(defn failure-view [error]
  (println error)
  [:div "Got an error when fetching albums."])

(rf/reg-event-fx
 ::fetch
 (fn [_ _]
   {:http-xhrio {:uri     BACKEND_URL
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

(defn view []
  (let [albums (rf/subscribe [::albums])]
    (fn []
      [:<>
       [:button {:on-click #(rf/dispatch [::fetch])} "Fetch albums"]
       (if (:results @albums)
         [success-view (:results @albums)]
         [failure-view (:error @albums)])])))
