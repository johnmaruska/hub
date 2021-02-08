(ns hub.discljord.role
  (:require
   [clojure.string :as string]
   [discljord.formatting :as f :refer [mention-user mention-role]]
   [discljord.messaging :as m]
   [hub.discljord.util :as util]
   [hub.util :refer [find-by]]))

;;;; query

(defn all [bot event]
  (let [result @(m/get-guild-roles! (:message-ch bot)
                                    (:guild-id event))]
    (filter :mentionable result)))


(defn list-all [bot event]
  (let [response (str "All roles in the server are: "
                      (util/display-seq (map :name (all bot event))))]
    (util/reply bot event response)))

(defn list-authors [bot event]
  (let [roles    @(m/get-guild-member! (:message-ch bot)
                                       (:guild-id event)
                                       (-> event :author :id))
        response (str (mention-user (:author event)) "'s roles are: "
                      (util/display-seq (map :name roles)))]
    (util/reply bot event response)))

(defn create [bot event]
  (let [role-name (string/replace (:content event) #"^!role new " "")
        result    @(m/create-guild-role! (:message-ch bot)
                                         (:guild-id event)
                                         :name role-name
                                         :hoist true
                                         :mentionable true)]
    (util/reply bot event (str "Created role " (mention-role result)))))

(defn add-to-author [bot event]
  (let [role-name (string/replace (:content event) #"^!role join " "")]
    (if-let [role-id (:id (find-by :name role-name (all bot event)))]
      (do @(m/add-guild-member-role! (:message-ch bot)
                                     (:guild-id event)
                                     (-> event :author :id)
                                     role-id)
          (util/reply bot event
                      (str "Added role " role-name " to user "
                           (mention-user (:author event)))))
      (util/reply bot event
                  (str "Role " role-name " could not be found.")))))

(defn remove-from-author [bot event]
  (let [role-name (string/replace (:content event) #"^!role leave " "")]
    (if-let [role-id (:id (find-by :name role-name (all bot event)))]
      (do @(m/remove-guild-member-role! (:message-ch bot)
                                        (:guild-id event)
                                        (-> event :author :id)
                                        role-id)
          (util/reply bot event (str "Removed role " role-name
                                     " from user " (mention-user (:author event)))))
      (util/reply bot event (str "No role by the name " role-name
                                 " was assigned to " (mention-user (:author event)))))))

;; TODO: make this pull from docstrings
(def help-message
  "The `role` extension is used for basic role management.
This is intended for making @ groups for anyone interested in gamez.

Commands are as follows:

  !role list         - list all mentionable roles in the server
  !role list-mine    - list all mentionable roles associated with your user
  !role new [ROLE]   - Create a new role named the entirety of the following message
  !role join [ROLE]  - add a role to your user. No need to prepend with @ just use exact string
  !role leave [ROLE] - remove a role from your user. No need to prepend with @ just use exact string")

(defn help [bot event]
  (util/reply bot event help-message))

;;;; display

(defn guild-message? [event]
  (boolean (:guild-id event)))

(def role-commands
  {"!role help"      help
   "!role list"      list-all
   "!role list-mine" list-authors
   "!role new"       create
   "!role join"      add-to-author
   "!role leave"     remove-from-author})
