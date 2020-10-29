(ns hub.discljord.spec.user
  (:require
   [hub.discljord.spec.primitives :as p]
   [malli.util :as mu]))

;; https://discord.com/developers/docs/resources/user#user-object
(def user
  (mu/merge
   [:map
    [:id p/snowflake]
    [:username string?]
    [:discriminator p/discriminator]
    [:avatar [:or nil? string?]]]
   (mu/optional-keys
    [:map
     [:bot boolean?]
     [:system boolean?]
     [:mfa-enabled boolean?]
     [:locale string?]
     [:verified boolean?]
     [:email [:or nil? string?]]
     [:flags integer?]
     [:premium-type integer?]
     [:public-flags integer?]])))
