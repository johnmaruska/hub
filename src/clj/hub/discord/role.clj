(ns hub.discord.role
  (:require
   [discord.bot :as bot]
   [discord.http :as http]
   [hub.discord.util :as util :refer [format-roles strip-command]]
   [hub.util :refer [find-by]]))

(bot/defextension role [client message]
  (:help
   "Prints out help message for game commands."
   (bot/say "The `role` extension is used for basic role management.
This is intended for making @ groups for anyone interested in gamez.

Commands are as follows:

  !role list         - list all mentionable roles in the server
  !role list-mine    - list all mentionable roles associated with your user
  !role new [ROLE]   - Create a new role named the entirety of the following message
  !role join [ROLE]  - add a role to your user. No need to prepend with @ just use exact string
  !role leave [ROLE] - remove a role from your user. No need to prepend with @ just use exact string"))
  (:list
   "Show names of all roles"
   (let [guild (get-in message [:channel :guild-id])
         roles (filter :mentionable (http/get-roles client guild))]
     (bot/say (str "All roles in the server are: " (format-roles roles)))))
  (:list-mine
   "Show names of all roles assigned to member."
   (let [guild  (get-in message [:channel :guild-id])
         author (:author message)
         roles  (util/get-user-roles client guild (:id author))]
     (bot/say (str (:username author) "'s roles are: " (format-roles roles)))))
  (:new
   "Creates a new role for players to join."
   (let [guild  (get-in message [:channel :guild-id])
         author (:author message)
         role   (strip-command (:content message))]
     (util/create-role client guild
                       :name role
                       :hoist true
                       :mentionable true)
     (bot/say (str "Created role @" role))))
  (:join
   "Allows a member to add themselves to a role."
   (let [guild     (get-in message [:channel :guild-id])
         role-name (strip-command (:content message))
         author    (:author message)]
     (if-let [role (util/get-role-by-name client guild role-name)]
       (do
         (util/add-user-role client guild (:id author) (:id role))
         (bot/say (str "Added role " role-name " to user " (:username author))))
       (bot/say (str "Role " role-name " could not be found.")))))
  (:leave
   "Allow a member to remove themselves from a role."
   (let [guild     (get-in message [:channel :guild-id])
         role-name (strip-command (:content message))
         author    (:author message)]
     (if-let [role (find-by :name role-name (util/get-user-roles client guild (:id author)))]
       (do (util/remove-user-role client guild (:id author) (:id role))
           (bot/say (str "Removed role " role-name
                         " from user " (:username author))))
       (bot/say (str "No role by the name " role-name
                     " was assigned to " (:username author)))))))
