(ns hub.albums
  (:require
   [ajax.edn :as ajax]
   [clojure.string :as string]
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

(defn lower-includes?
  "Use as a case-insensitive includes?, does so by forcing both to lower-case"
  [s substr]
  (string/includes? (string/lower-case s)
                    (string/lower-case substr)))

(defn labelled-input [{:keys [id display type name] :as opts}]
  [:<>
   [:label {:for id} display]
   [:input opts]])

;; TODO: better spacing
(defn add-album []
  [:form {:action BACKEND_URL :method "POST"}
   [labelled-input {:type "text" :name "artist" :id "artist" :display "Artist"}]
   [:br]
   [labelled-input {:type "text" :name "release" :id "release" :display "Release"}]
   [:br]
   (for [fmt FORMATS]
     (do
       (println fmt)
       [labelled-input {:type "radio" :name "ownership"
                        :key fmt :id fmt :display fmt :value fmt}]))
   [:br]
   [labelled-input {:type "submit" :name "submit" :id "submit" :display "Submit"}]])

(defn success-view [albums]
  (let [artist-filter (r/atom "")
        album-filter  (r/atom "")
        format-filter (r/atom "")
        update!       #(reset! %1 (.. %2 -target -value))]
    (fn []
      (let [shown-albums (->> albums
                              (filter #(lower-includes? (:artist %) @artist-filter))
                              (filter #(lower-includes? (:release %) @album-filter))
                              (filter #(lower-includes? (:ownership %) @format-filter)))]
        [:table
         [:thead
          [:tr
           ;; TODO: use CSS instead of :br
           [:th "Artist"  [:br]
            [text-input {:id        "album--artist"
                         :on-change #(update! artist-filter %)}]]
           [:th "Release" [:br]
            [text-input {:id        "album--name"
                         :on-change #(update! album-filter %)}]]
           [:th "Format"  [:br]
            [dropdown {:id        "album--format"
                       :on-change #(update! format-filter %)}
             (conj FORMATS "")]]]]
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
 (fn [_ [_ query-params]]
   {:http-xhrio {:uri     BACKEND_URL
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
   {:Db (assoc-in db [:inventory :albums :error] resp)}))

(rf/reg-sub
 ::albums
 (fn [db _]
   (get-in db [:inventory :albums])))

(defn view []
  (let [albums (rf/subscribe [::albums])]
    (fn []
      [:<>
       [add-album]
       [:button {:on-click #(rf/dispatch [::fetch])} "Fetch albums"]
       (if (:results @albums)
         [success-view (:results @albums)]
         [failure-view (:error @albums)])])))
