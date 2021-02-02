(ns hub.spotify.auth
  "https://developer.spotify.com/documentation/general/guides/authorization-guide/"
  (:require [clj-http.client :as http])
  (:import (java.util Base64)))

;; TODO: from more secure location than local envvars
(def client-secret (System/getenv "SPOTIFY_SECRET"))
(def client-id (System/getenv "SPOTIFY_ID"))

(defn encode [s]
  (.encodeToString (Base64/getEncoder) (.getBytes s)))

(defn post-basic-auth [form-params]
  (let [creds (str client-id ":" client-secret)
        auth  (str "Basic " (encode creds))]
    (:body (http/post "https://accounts.spotify.com/api/token"
                      {:form-params form-params
                       :basic-auth [client-id client-secret]
                       :as :json}))))

(defn client-credentials
  "Request an access token via the client credentials flow.
  Only for public data."
  []
  (:access_token (post-basic-auth {:grant_type "client_credentials"})))
