(ns hub.spotify.auth
  "https://developer.spotify.com/documentation/general/guides/authorization-guide/"
  (:require
   [clj-http.client :as http]
   [clojure.string :as string]
   [ring.util.codec :as codec])
  (:import (java.util Base64)))

;; TODO: from more secure location than local envvars
(def client-secret (System/getenv "SPOTIFY_SECRET"))
(def client-id (System/getenv "SPOTIFY_ID"))
;; TODO: pull from config depending on deploy location
;; TODO: pull path from a bidirectional library instead of hard code
(def redirect-uri "http://127.0.0.1:4000/en/spotify/callback")
(def scope ["playlist-read-private"
            "playlist-read-collaborative"
            "playlist-modify-public"
            "playlist-modify-private"])

(def token (atom nil))

(defn encode [s]
  (.encodeToString (Base64/getEncoder) (.getBytes s)))

(defn url [endpoint]
  (str "https://accounts.spotify.com" endpoint))

(defn post-basic-auth [form-params]
  (let [creds (str client-id ":" client-secret)
        auth  (str "Basic " (encode creds))]
    (:body (http/post (url "/api/token")
                      {:form-params form-params
                       :basic-auth [client-id client-secret]
                       :as :json}))))

(defn client-credentials
  "Request an access token via the client credentials flow.
  Only for public data."
  []
  (post-basic-auth {:grant_type "client_credentials"}))

(defn authorization-code
  "Request an access token via the authorization code flow using the code
  provided by the /authorize endpoint."
  [auth-code & [state]]
  (let [result (post-basic-auth {:grant_type   "authorization_code"
                                 :code         auth-code
                                 :redirect_uri redirect-uri})]
    (reset! token result)
    result))

(defn refresh-token
  "Refresh a given access token acquired previously."
  [api-token]
  (post-basic-auth {:grant_type "refresh_token"
                    :refresh_token (:refresh_token api-token)}))

(def auth-url
  (str (url "/authorize") "?"
       (codec/form-encode {:response_type "code"
                           :client_id     client-id
                           :scope         scope
                           :redirect_uri  redirect-uri
                           ;; TODO: supply a `state` for CSRF protection
                           })))
