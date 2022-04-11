(ns hub.conway
  (:require
   [hub.conway.game :as game]
   [re-frame.core :as rf]))

(def delay-ms 200)
(def default-height 15)
(def default-width 15)
(def default-db {::game (game/random-seed default-height default-width)})

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

(rf/reg-event-db
 ::step-game
 (fn [db _]
   (update db ::game game/play-round)))

(rf/reg-sub
 ::game
 (fn [db _]
   (::game db)))

(defn view []
  (let [game (rf/subscribe [::game])]
    (fn []
      [:div {:class "conway"}
       [:button {:on-click #(rf/dispatch [::step-game])} "Step Game"]
       [grid @game]])))
