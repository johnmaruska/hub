(ns ^:figwheel-hooks hub.core
  (:require
   [day8.re-frame.http-fx]  ; enables :http-xhrio fx
   [hub.albums :as albums]
   [hub.conway :as conway]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]))

(defn current-page []
  [albums/view])

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   conway/default-db))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rf/dispatch-sync [:initialize-db])
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [#'current-page] root-el)))

;; ugh I don't like this
(defonce start-up (do (mount-root) true))
