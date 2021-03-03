(ns hub.inventory.spec)

(def album
  [:map
   [:artist string?]
   [:release string?]
   ;; TODO: one of Vinyl, Digital, CD
   [:ownership string?]])
