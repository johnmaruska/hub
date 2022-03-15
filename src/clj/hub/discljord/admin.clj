(ns hub.discljord.admin
  (:require
   [hub.discljord.util :as util]
   [discljord.messaging :as m]))

(def canned-reply
  {:could-not-parse (str "Could not parse nickname change. Format must be"
                         " `@mention New Nickname`")
   :working         "https://giphy.com/gifs/9K2nFglCAQClO"})

(defn working [bot event]
  (util/reply bot event (:working canned-reply)))


(defn change-nickname* [bot event user-id nickname]
  (m/modify-guild-member! (:message-ch bot)
                          (:guild-id event)
                          user-id
                          :nick nickname))

(defn change-nickname [bot event]
  (let [[_ user-id nickname] (re-find #"<@!(\d{18})> (.+)" (:content event))]
    (cond
      (not (and user-id nickname))
      (util/reply bot event (:could-not-parse canned-reply))

      :else
      ;; result is coming back as (= status 204) => false, unknown as to why.
      ;; I _assume_ it's a permissions things but discljord makes that hard to see
      (util/reply bot event "Got nickname request. Not yet implemented.")
      #_
      (let [result @(change-nickname* bot event user-id nickname)]
        (util/reply bot event (str "change-nickname result:" result))))))
