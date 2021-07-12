(ns ^:figwheel-hooks hub.core
  (:require
   [day8.re-frame.http-fx]  ; enables :http-xhrio fx
   [hub.albums :as albums]
   [hub.db :as db]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]))

(defn current-page []
  [:div
   [:button {:on-click (fn [e] (rf/dispatch [::albums/fetch]))}
    "Fetch albums"]
   [albums/view]])

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [#'current-page] root-el)))

;; ugh I don't like this
(defonce start-up (do (mount-root) true))
