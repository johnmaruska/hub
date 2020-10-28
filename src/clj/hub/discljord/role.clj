(ns hub.discljord.role
  (:require
   [discljord.messaging :as m]
   [hub.discljord.util :as util]))

;;;; query

(defn all [bot event]
  (let [result @(m/get-guild-roles! (:message-ch bot)
                                    (:guild-id event))]
    (filter :mentionable result)))


(defn list-all [bot event]
  (let [response (str "All roles in the server are: "
                      (util/format-seq (map :name (all bot event))))]
    (reply bot event response)))

(defn list-authors [bot event]
  (let [roles    @(m/get-guild-member! (:message-ch bot)
                                       (:guild-id event)
                                       (-> event :author :id))
        response (str (mention-user (:author event)) "'s roles are: "
                      (util/format-seq (map :name roles)))]
    (reply bot event response)))

(defn create [bot event]
  (let [role-name (string/replace (:content event) #"^!role new " "")
        result    @(m/create-guid-role (:message-ch bot)
                                       (:guild-id event)
                                       :name role-name
                                       :hoist true
                                       :mentionable true)]
    (util/reply bot event (str "Created role " (mention-role result)))))

(defn add-to-author [bot event]
  (let [role-name (string/replace (:content event) #"^!role join " "")]
    (if-let [role-id (:id (util/find-by :name role-name (all bot event)))]
      (do @(m/add-guild-member-role (:message-ch bot)
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
    (if-let [role-id (:id (util/find-by :name role-name (all bot event)))]
      (do @(m/remove-guild-member-role (:message-ch bot)
                                       (:guild-id event)
                                       (-> event :author :id)
                                       role-id)
          (reply bot event (str "Removed role " role-name
                                " from user " (mention-user (:author event)))))
      (reply bot event (str "No role by the name " role-name
                            " was assigned to " (mention-user (:author event)))))))

(def help-message
  "The `role` extension is used for basic role management.
This is intended for making @ groups for anyone interested in gamez.

Commands are as follows:

  !role list         - list all mentionable roles in the server
  !role list-mine    - list all mentionable roles associated with your user
  !role new [ROLE]   - Create a new role named the entirety of the following message
  !role join [ROLE]  - add a role to your user. No need to prepend with @ just use exact string
  !role leave [ROLE] - remove a role from your user. No need to prepend with @ just use exact string")

;;;; display

(defn guild-message? [event]
  (boolean (:guild-id event)))

;; I'd rather do this unix-style.
;; "!role" command dispatches to "list" "list-mine" etc dispatches
(defn handle [bot event]
  (when (guild-message? event)
    (util/command (:content event)
      "!role help"      (util/reply bot event help-message)
      "!role list"      (list-all bot event)
      "!role list-mine" (list-authors bot event)
      "!role new"       (create bot event)
      "!role join"      (add-to-author bot event)
      "!role leave"     (remove-from-author bot event))))
