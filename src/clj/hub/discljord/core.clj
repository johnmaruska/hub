(ns hub.discljord.core
  (:require
   [clojure.core.async :as a]
   [discljord.connections :as c]
   [discljord.events :as e]
   [discljord.messaging :as m]))

(def token (System/getenv "DISCORD_TOKEN"))

(defn start! []
  (let [event-ch (a/chan 100)]
    {:event-ch      event-ch
     :connection-ch (c/connect-bot! token event-ch)
     :message-ch    (m/start-connection! token)}))

(defn stop! [bot]
  (m/stop-connection! (:message-ch bot))
  (c/disconnect-bot!  (:connection-ch bot))
  (a/close! (:event-ch bot)))

(defn handle-message-create [data]
  )

(defn handle-event [[event-type event-data]]
  (println "NEW EVENT!")
  (println "Event type:" event-type)
  (println "Event data:" (pr-str event-data)))

(defn spin-forever! [bot]
  (try
    (loop []
      (handle-message (a/<!! (:event-ch bot)))
      (recur))
    (finally (stop! bot))))


(def data
  {:description nil
   :large false
   :preferred-locale "en-US"
   :features []
   :application-id nil
   :emojis []
   :member-count 9
   :splash nil
   :max-members 100000
   :public-updates-channel-id nil
   :channels [{:type 4
               :position 0
               :permission-overwrites []
               :name "Text Channels"
               :id "763910503398506547"}
              {:type 4
               :position 0
               :permission-overwrites []
               :name "Voice Channels"
               :id "763910503398506548"}
              {:last-message-id "763933444438097921"
               :permission-overwrites []
               :name "general"
               :type 0
               :topic nil
               :rate-limit-per-user 0
               :id "763910503398506549"
               :parent-id "763910503398506547"
               :position 0}
              {:user-limit 0
               :type 2
               :position 0
               :permission-overwrites []
               :parent-id "763910503398506548"
               :name "General"
               :id "763910503398506550"
               :bitrate 64000}
              {:last-message-id nil
               :permission-overwrites []
               :name "bot-playground"
               :type 0
               :topic nil
               :nsfw false
               :rate-limit-per-user 0
               :id "763910536613724201"
               :parent-id "763910503398506547"
               :position 1}]
   :system-channel-flags 3
   :afk-timeout 300
   :afk-channel-id nil
   :name "Arch Lisp"
   :premium-tier 0
   :vanity-url-code nil
   :roles [{:mentionable false
            :permissions 104320577
            :color 0
            :name "@everyone"
            :permissions-new "104320577"
            :id "763910503398506546"
            :managed false
            :position 0
            :hoist false}
           {:tags {:bot-id "755872716531171521"}
            :mentionable false
            :permissions 8
            :color 0
            :name "rolycoly-bot-unholy"
            :permissions-new "8"
            :id "763912014602174476"
            :managed true
            :position 1
            :hoist false}
           {:tags {:bot-id "763923783698350101"}
            :mentionable false
            :permissions 8
            :color 0
            :name "chris-app"
            :permissions-new "8"
            :id "763924074556031038"
            :managed true
            :position 1
            :hoist false}]
   :unavailable false
   :mfa-level 0
   :joined-at "2020-10-08T23:53:37.135120+00:00"
   :default-message-notifications 1
   :explicit-content-filter 0
   :max-video-channel-users 25
   :icon nil
   :voice-states []
   :guild-hashes {:version 1
                  :roles {:omitted false
                          :hash "GNq7IVFFaxg"}
                  :metadata {:omitted false
                             :hash "KvccSNHOaws"}
                  :channels {:omitted false
                             :hash "JepaSBUeqWY"}}
   :verification-level 0
   :region "us-central"
   :rules-channel-id nil
   :lazy true
   :id "763910503398506546"
   :owner-id "126811887999516672"
   :banner nil
   :premium-subscription-count 0
   :presences [{:user {:id "197145586317787136"}
                :status "online"
                :game nil
                :client-status {:desktop "online"}
                :activities []}
               {:user {:id "276215177899147265"}
                :status "online"
                :game nil
                :client-status {:desktop "online"}
                :activities []}
               {:user {:id "755872716531171521"}
                :status "online"
                :game nil
                :client-status {:web "online"}
                :activities []}
               {:user {:id "763923783698350101"}
                :status "online"
                :game {:type 0
                       :name "Say hello!"
                       :id "ec0b28a579ecb4bd"
                       :created-at 1602205092329}
                :client-status {:web "online"}
                :activities [{:type 0
                              :name "Say hello!"
                              :id "ec0b28a579ecb4bd"
                              :created-at 1602205092329}]}]
   :system-channel-id "763910503398506549"
   :members [{:user {:username "JohnOfPatmos"
                     :id "126811887999516672"
                     :discriminator "4343"
                     :avatar "d689788d101d986c8f73305d6a2c9543"}
              :roles []
              :mute false
              :joined-at "2020-10-08T23:47:36.789000+00:00"
              :hoisted-role nil
              :deaf false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:53:12.824546+00:00"
              :user {:username "travv0"
                     :public-flags 0
                     :id "187742421143126017"
                     :discriminator "1312"
                     :avatar "569173096c6153120f17b370d101cc15"}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:48:18.243207+00:00"
              :user {:username "komcrad"
                     :id "197145586317787136"
                     :discriminator "5421"
                     :avatar "d976df009ea4a9885f3a0fde73ed1806"}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:48:13.022074+00:00"
              :user {:username "cgore"
                     :id "276215177899147265"
                     :discriminator "0920"
                     :avatar "c118b2600a41891a8ed80cc29630aa82"}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:52:32.068721+00:00"
              :user {:username "idas"
                     :id "384033737136144405"
                     :discriminator "6928"
                     :avatar nil}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick "hackeryarn"
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:50:56.497617+00:00"
              :user {:username "hackeryarn"
                     :public-flags 0
                     :id "391074271071305728"
                     :discriminator "2765"
                     :avatar "83c9b60e5a3564b6d72c5650530a5946"}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles ["763912014602174476"]
              :joined-at "2020-10-08T23:53:37.135120+00:00"
              :user {:username "rolycoly-bot-unholy"
                     :id "755872716531171521"
                     :discriminator "5981"
                     :bot true
                     :avatar nil}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles []
              :joined-at "2020-10-08T23:48:20.608063+00:00"
              :user {:username "hackeryarn"
                     :public-flags 0
                     :id "763910654276009994"
                     :discriminator "3977"
                     :avatar nil}
              :mute false}
             {:premium-since nil
              :deaf false
              :is-pending false
              :nick nil
              :hoisted-role nil
              :roles ["763924074556031038"]
              :joined-at "2020-10-09T00:41:32.522939+00:00"
              :user {:username "chris-bot"
                     :id "763923783698350101"
                     :discriminator "0359"
                     :bot true
                     :avatar nil}
              :mute false}]
   :discovery-splash nil})
