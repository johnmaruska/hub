(ns hub.discljord.spec.channels
  "specs for the Channels Resource as specified by Discord docs.
  https://discord.com/developers/docs/resources/channel"
  (:require
   [hub.discljord.spec.emoji :refer [emoji]]
   [hub.discljord.spec.guild :refer [member]]
   [hub.discljord.spec.primitives :as p :refer [snowflake timestamp]]
   [hub.discljord.spec.user :refer [user]]
   [malli.util :as mu]))

;; https://discord.com/developers/docs/resources/channel#attachment-object
(def attachment
  [:map
   [:id snowflake]
   [:filename string?]
   [:size integer?]
   [:url string?]
   [:proxy-url string?]
   [:height [:or nil? integer?]]
   [:width [:or nil? integer?]]])

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-thumbnail-structure
(def embed-thumbnail
  (mu/optional-keys
   [:map
    [:url string?]
    [:proxy-url string?]
    [:height integer?]
    [:width integer?]]))

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-video-structure
(def embed-video
  (mu/optional-keys
   [:map
    [:url string?]
    [:height string?]
    [:width string?]]))

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-image-structure
(def embed-image
  (mu/optional-keys
   [:map
    [:url string?]
    [:proxy-url string?]
    [:height integer?]
    [:width integer?]]))

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-provider-structure
(def embed-provider
  (mu/optional-keys
   [:map
    [:name string?]
    [:url string?]]))

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-author-structure
(def embed-author
  (mu/optional-keys
   [:map
    [:name string?]
    [:url string?]
    [:icon-url string?]
    [:proxy-icon-url string?]]))

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-footer-structure
(def embed-footer
  [:map
   [:text string?]
   [:icon-url {:optional true} string?]
   [:proxy-icon-url {:optional true} string?]])

;; https://discord.com/developers/docs/resources/channel#embed-object-embed-field-structure
(def embed-field
  [:map
   [:name string?]
   [:value string?]
   [:inline {:optional true} boolean?]])

;; https://discord.com/developers/docs/resources/channel#embed-object
(def embed
  (mu/optional-keys
   [:map
    [:title string?]
    ;; https://discord.com/developers/docs/resources/channel#embed-object-embed-types
    [:type [:enum "rich" "image" "video" "gifv" "article" "link"]]
    [:description string?]
    [:url string?]
    [:timestamp? timestamp]
    [:color integer?]
    [:footer embed-footer]
    [:image embed-image]
    [:thumbnail embed-thumbnail]
    [:video embed-video]
    [:provider embed-provider]
    [:author embed-author]
    [:fields [:vector embed-field]]]))

;; https://discord.com/developers/docs/resources/channel#channel-object-channel-types
(def channel-types
  {0 :GUILD_TEXT
   1 :DM
   2 :GUILD_VOICE
   3 :GROUP_DM
   4 :GUILD_CATEGORY
   5 :GUILD_NEWS
   6 :GUILD_STORE})

;; https://discord.com/developers/docs/resources/channel#channel-mention-object
(def channel-mention
  [:map
   [:id snowflake]
   [:guild-id snowflake]
   [:type (vec (concat [:enum] (keys channel-types)))]
   [:name string?]])

;; https://discord.com/developers/docs/resources/channel#reaction-object
(def reaction
  [:map
   [:count integer?]
   [:me boolean?]
   [:emoji emoji]])

;; https://discord.com/developers/docs/resources/channel#message-object-message-activity-structure
(def message-activity
  [:map
   [:type integer?]
   [:party-id {:optional true} string?]])

;; https://discord.com/developers/docs/resources/channel#message-object-message-application-structure
(def message-application
  [:map
   [:id snowflake]
   [:cover-image {:optional true} string?]
   [:description string?]
   [:icon [:or nil? string?]]
   [:name string?]])

;; https://discord.com/developers/docs/resources/channel#message-object-message-reference-structure
(def message-reference
  [:map
   [:message-id {:optional true} snowflake]
   [:channel-id snowflake]
   [:guild-id {:optional true} snowflake]])

;; https://discord.com/developers/docs/resources/channel#message-object
(def message
  (mu/merge
   [:map
    [:id snowflake]
    [:channel-id snowflake]
    [:author user]
    [:content string?]
    [:timestamp timestamp]
    [:edited-timestamp [:or nil? timestamp]]
    [:tts boolean?]
    [:mention-everyone boolean?]
    [:mentions [:vector (mu/assoc user :member member)]]
    [:mention-roles [:vector snowflake]]
    [:attachments [:vector attachment]]
    [:embeds [:vector embed]]
    [:pinned boolean?]
    [:type integer?]]
   (mu/optional-keys
    [:map
     [:guild-id snowflake]
     [:member member]  ; partial guild member object
     [:mention-channels [:vector channel-mention]]
     [:reactions [:vector reaction]]
     [:nonce [:or integer? string?]]
     [:webhook-id snowflake]
     [:activity message-activity]
     [:application message-application]
     [:message-reference message-reference]
     ;; https://discord.com/developers/docs/resources/channel#message-object-message-flags
     [:flags integer?]])))
