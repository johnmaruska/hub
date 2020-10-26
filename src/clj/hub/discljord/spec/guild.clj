(ns hub.discljord.spec.guild
  "specs for the Guild Resource as specified by Discord docs.
  https://discord.com/developers/docs/resources/guild#guild-member-object"
  (:require
   [hub.discljord.spec.primitives :as p]
   [hub.discljord.spec.user :refer [user]]
   [malli.util :as mu]))

;; https://discord.com/developers/docs/resources/guild#guild-member-object
(def member
  (mu/merge
   [:map
    [:nick [:or nil? string?]]
    [:roles [:vector p/snowflake]]
    [:joined-at p/timestamp]
    [:deaf boolean?]
    [:mute boolean?]]
   (mu/optional-keys
    [:map
     [:user user]
     [:premium-since p/timestamp]])))
