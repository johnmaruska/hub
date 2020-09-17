(ns hub.discord.util
  "Helper utilities for Discord. Some of these could be added to the library."
  (:require
   [discord.http :as http]))

(defn get-role-by-name [auth guild role-name]
  (->> (http/get-roles auth guild)
       (filter (fn [role] (= role-name (:name role))))
       (first)))

;;; not sure why these aren't in discord/http.clj
(defn create-role [auth guild & {:keys [name permissions color hoist mentionable] :as params}]
  (http/discord-request :create-role auth
                        :guild guild
                        :json  params))

(defn add-user-role [auth guild member role]
  (http/discord-request :add-user-role auth
                        :guild  guild
                        :member member
                        :role   role))

(defn remove-user-role [auth guild member role]
  (http/discord-request :remove-user-role auth
                        :guild  guild
                        :member member
                        :role   role))

(defn get-user-roles
  "List of roles IDs assigned to a `user-id`. Pulls from server."
  [client guild user-id]
  (let [user     (http/get-guild-member client guild user-id)
        role-ids (into #{} (:roles user))]
    (filter (fn [role] (contains? role-ids (:id role)))
            (http/get-roles client guild))))
