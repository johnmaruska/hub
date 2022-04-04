(ns hub.spotify.token
  "Interactions with Spotify Authorization Flow API token.
  Assumes only one token will be present and will be stored on a local file
  for use after restarts to prevent reauthorization.

  When this moves into a full VM we'll need external storage to handle restarts
  of the full VM and not just of the Java VM."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def filename ".tokens/authorization-code.edn")

(def token (atom nil))

(defn fetch! []
  (when (.exists (io/file filename))
    (let [stored (edn/read-string (slurp filename))]
      (reset! token stored))))

(defn api-token []
  (or @token (fetch!)))

(defn refresh-token?
  "Check that the token has a refresh_token field.

  There are two types of tokens we can get depending on authorization flow. One
  has a refresh_token field, and is the more permissive token. The other does
  not and is more restricted in its uses. We need to check this in a couple
  spots."[]
  (boolean (:refresh_token (api-token))))

(defn persist!
  "Stores token in local atom and in `filename`, returns passed value."
  [new-token]
  ;; TODO: protect against error bodies. match schema, pass through if no match
  (spit filename new-token)
  (reset! token new-token))
