(ns hub.conway
  (:require
   [hub.conway.game :as game]
   [re-frame.core :as rf]))

(def delay-ms 200)
(def default-height 15)
(def default-width 15)
(def default-db {::game     (game/random-seed default-height default-width)
                 ::autoplay false})

(def display-size "1em")

(defn enumerate [xs]
  (map-indexed (fn [idx elem] [idx elem]) xs))

(rf/reg-event-db
 ::toggle-cell
 (fn [db [_ coord]]
   (update db ::game game/toggle-cell coord)))

(defn cell [coord val]
  [:div {:on-click #(rf/dispatch [::toggle-cell coord])
         :style {:background-color (if (game/alive? val) "black" "white")
                 :width display-size
                 :height display-size}}])

(defn grid [game]
  [:table
   [:tbody
    (doall
     (for [[y row] (enumerate game)]
       ^{:key (str "y=" y)}
       [:tr (doall
             (for [[x val] (enumerate row)]
               ^{:key (str "x=" x)}
               [:td [cell [x y] val]]))]))]])

(rf/reg-sub
 ::game
 (fn [db _]
   (::game db)))

(rf/reg-event-db
 ::step-game
 (fn [db _]
   (update db ::game game/play-round)))

(rf/reg-sub
 ::autoplay
 (fn [db _]
   (::autoplay db)))

;; step board and handle loop for autoplay, which is a side effect
(rf/reg-event-fx
 ::autoplay
 (fn [{db :db} _]
   (if (::autoplay db)
     {:fx [[:dispatch [::step-game]]]
      ::sleep-then-run [#(rf/dispatch [::autoplay]) delay-ms]}
     {:fx [[:dispatch [::step-game]]]})))

;; this is that side effect
(rf/reg-fx
 ::sleep-then-run
 (fn [[thunk delay]]
   (js/setTimeout thunk delay)))

;; user interaction to escape the forever loop
(rf/reg-event-fx
 ::toggle-autoplay
 (fn [{db :db} [_ target]]
   (let [autoplay (not (.-checked target))]
     {:db (assoc db ::autoplay autoplay)
      :fx [[:dispatch [::autoplay]]]})))

(defn view []
  (let [game      (rf/subscribe [::game])
        autoplay? (rf/subscribe [::autoplay])]
    (fn []
      [:div {:class "conway"}
       [:button {:on-click #(rf/dispatch [::step-game])} "Step Game"]
       [:div
        "Autoplay"
        [:input {:type      "checkbox"
                 :checked   @autoplay?
                 :on-change #(rf/dispatch [::toggle-autoplay (.-target %)])}]]
       [grid @game]])))
