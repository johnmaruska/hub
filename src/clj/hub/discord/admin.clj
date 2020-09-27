(ns hub.discord.admin
  (:require
   [discord.bot :as bot]
   [discord.http :as http]))

(bot/defextension admin [client message]
  (:clean-new-roles
   "Removes all roles with name `new role` which are sometimes are accidentally created."
   (let [guild (get-in message [:channel :guild-id])]
     (doseq [{:keys [name id] :as role} (http/get-roles client guild)]
       (when (= "new role" name)
         (http/discord-request :delete-role client
                               :guild guild
                               :role  id)
         (bot/say (str "Deleted role" name "id" id))))
     (bot/say (str "Remaining roles: "
                   (->> (http/get-roles client guild)
                        (map #(select-keys % [:id :name]))
                        (into []))))))
  (:working
   "Posts the Star Wars Episode 1 'It's working' gif in the channel"
   (bot/say "https://giphy.com/gifs/9K2nFglCAQClO")))
