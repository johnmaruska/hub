(ns hub.discljord.spec.emoji
  (:require
   [hub.discljord.spec.primitives :as p]
   [hub.discljord.spec.user :refer [user]]
   [malli.util :as mu]))

;; https://discord.com/developers/docs/resources/emoji#emoji-object
(def emoji
  (mu/merge
   [:map
    [:id [:or nil? p/snowflake]]
    [:name [:or nil? string?]]]
   (mu/optional-keys
    [:map
     [:roles [:vector p/snowflake]]
     [:user user]
     [:require-colons boolean?]
     [:managed boolean?]
     [:animated boolean?]
     [:available boolean?]])))
