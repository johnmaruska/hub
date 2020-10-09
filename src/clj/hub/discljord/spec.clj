(ns hub.discljord.spec
  (:require [malli.core :as m]))

(defn timestamp? [s]
  (and (string? s) ...))

(def unix-timestamp? int?)

(defn int-string? [s]
  (and (string? s) (re-matches #"\d*")))

(defn discriminator? [s]
  (and (string? s) (re-matches #"\d{4}")))

(defn hex-string? [s]
  (and (string? s) (re-matches #"[a-zA-Z\d]*" s)))

(defn id? [s]
  (and (string? s) (re-matches #"\d{18}" s)))

(defn or-nil [f]
  (fn [x] (or (f x) (nil? x))))

(def author?
  (m/validator
   [:map
    [:avatar (or-nil? hex-string?)]
    [:discriminator discriminator?]
    [:id id?]
    [:public-flags int?]
    [:username string?]]))

(def member?
  (m/validator
   [:map
    [:deaf boolean?]
    [:hoisted-role id?]
    [:joined-at timestamp?]
    [:mute boolean?]
    [:roles [:vector id?]]]))

(def status?
  (m/validator
   [:enum "online" ...]))

(def locale? string?)  ;; (m/validator [:enum "en-US" ...])

(def guild?
  (m/validator
   [:map
    [:unavailable boolean?]
    [:id id?]]))

(def user?
  (m/validator
   [:map
    [:email (or-nil string?)]
    [:bot boolean?]
    [:username string?]
    [:id id?]
    [:avatar (or-nil? hex-string?)]
    [:mfa-enabled boolean?]
    [:flags int?]
    [:verified boolean?]
    [:discriminator discriminator?]]))

(def permission-overwrite?
  (m/validator
   [:map
    [:type string?]
    [:id id?]
    [:deny-new int-string?]
    [:deny int?]
    [:allow-new int-string?]
    [:allow int?]]))

(def channel?
  (m/validator
   (mu/union
    [:map
     [:type int?]
     [:position int?]
     [:permission-overwrites [:vector permission-overwrite?]]
     [:name string?]
     [:id id?]]
    (mu/optional-keys
     [:map
      [:last-message-id id?]
      [:name string?]
      [:topic (or-nil ...)]
      [:rate-limit-per-user int?]
      [:parent-id id?]
      [:user-limit int?]
      [:bitrate int?]]))))

;;; Event Types

(def message-create?
  (m/validator
   [:map
    [:attachments [:vector ...]]
    [:author author?]
    [:channel-id id?]
    [:content string?]
    [:edited-timestamp (or-nil timestamp?)]
    [:embeds [:vector ...]]
    [:flags int?]
    [:guild-id id?]
    [:id id?]
    [:member member?]
    [:mention-everyone boolean?]
    [:mentions [:vector ...]]
    [:mention-roles [:vector id?]]
    [:nonce id?]
    [:pinned boolean?]
    [:referenced-message (or-nil ...)]
    [:timestamp timestamp?]
    [:tts boolean?]
    [:type int?]]))

;; TODO: does {:optional true} mean no key or key exists with nil? or both?
(def presence-update?
  (m/validator
   [:map
    [:premium-since (or-nil ...)]
    [:nick          (or-nil ...)]
    [:game          (or-nil activity?)]
    [:roles [:vector ...]]
    [:guild-id id?]
    [:status status?]
    [:client-status client-status?]
    [:activities [:vector activity?]]
    [:user [:map [:id id?]]]]))

(def typing-start?
  (m/validator
   [:map
    [:user-id id?]
    [:member member?]
    [:timestamp unix-timestamp?]
    [:channel-id id?]
    [:guild-id id?]]))

(def ready?
  (m/validator
   [:map
    [:session-id hex-string?]
    [:v int?]
    [:user-settings
     [:map ...]]
    [:application [:map
                   [:id id?]
                   [:flags int?]]]
    [:shard [:vector int?]]
    [:private-channels [:vector ...]]
    [:guilds [:vector guild?]]
    [:presences [:vector ...]]
    [:relationships [:vector ...]]
    [:user user?]
    [:-trace string?]]))

(def role?
  (m/validator
   [:map
    [:mentionable boolean?]
    [:permissions int?]
    [:color int?]
    [:name string?]
    [:permissions-new int-string?]
    [:id id?]
    [:managed boolean?]
    [:position int?]
    [:hoist boolean?]
    [:tags {:optional true} [:map [:bot-id id?]]]]))

(def hash?
  (m/validator
   [:map
    [:omitted boolean?]
    [:hash string?]]))

(def guild-create?
  (m/validator
   [:map
    [:description (or-nil string?)]
    [:large boolean?]
    [:preferred-locale locale?]
    [:features [:vector ...]]
    [:application-id (or-nil ...)]
    [:emojis [:vector ...]]
    [:member-count int?]
    [:splash (or-nil ...)]
    [:max-members int?]
    [:public-updates-channel-id (or-nil ...)]
    [:channels channel?]
    [:system-channel-flags int?]
    [:afk-timeout int?]
    [:afk-channel-id (or-nil id?)]
    [:name string?]
    [:premium-tier int?]
    [:vanity-url-code (or-nil? string?)]
    [:roles [:vector role?]]
    [:unavailable boolean?]
    [:mfa-level int?]
    [:joined-at timestamp?]
    [:default-message-notifications int?]
    [:explicit-content-filter int?]
    [:max-video-channel-users int?]
    [:icon (or-nil ...)]
    [:voice-states [:vector ...]]
    [:guild-hashes
     [:map
      [:version int?]
      [:roles hash?]
      [:metadata hash?]
      [:channels hash?]]]
    [:verification-level int?]
    [:region string?]
    [:rules-channel-id (or-nil ...)]
    [:lazy boolean?]
    [:id id?]
    [:owner-id id?]
    [:banner (or-nil ...)]
    [:premium-subscription-count int?]
    [:presences [:vector presence?]]]))

(def activity?
  (m/validator
   [:map
    [:type int?]
    [:state string?]
    [:name string?]
    [:id string?]
    [:created-at unix-timestamp]]))

(def client-status?
  (m/validator
   (mu/optional-keys
    [:map
     [:mobile status?]
     [:desktop status?]])))

(def presence?
  (m/validator
   [:map
    [:user [:map [:id id?]]]
    [:status status?]
    [:game activity?]
    [:client-status client-status?]
    [:activities [:vector activity?]]
    [:members [:vector member?]]
    [:system-channel-id id?]
    [:discover-splash (or-nil ...)]]))
