(ns hub.spotify.auth
  "https://developer.spotify.com/documentation/general/guides/authorization-guide/"
  (:require
   [clj-http.client :as http]
   [clojure.string :as string]
   [hub.spotify.util :as util]
   [hub.spotify.token :as token]))

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

(def url (partial util/url :accounts))

(defn post-basic-auth [form-params]
  (:body (http/post (url "/api/token")
                    {:form-params form-params
                     :basic-auth [client-id client-secret]
                     :as :json})))

;;;; Client Credentials flow

(defn client-credentials
  "Request an access token via the client credentials flow.
  Only for public data."
  []
  (post-basic-auth {:grant_type "client_credentials"}))

;;;; Authorization Code flow

(defn refresh-token!
  "Refresh the stored API token"
  [api-token]
  (token/persist!
   (post-basic-auth {:grant_type "refresh_token"
                     :refresh_token (:refresh_token api-token)})))

(defn expired-token? [ex]
  (try
    (-> (util/parse-json (:body (ex-data ex)))
        (get-in [:error :error :message])
        (string/includes? "The access token expired"))
    (catch NullPointerException ex
      false)))

(defmacro with-refresh-token [& body]
  `(try
     ~@body
     (catch Exception ex#
       (if (expired-token? ex#)
         (do
           (refresh-token! @token)
           ~@body)
         (throw ex#)))))

(defn authorization-code
  "Request an access token via the authorization code flow using the code
  provided by the /authorize endpoint."
  [auth-code & [state]]
  (token/persist!
   (post-basic-auth {:grant_type   "authorization_code"
                     :code         auth-code
                     :redirect_uri redirect-uri})))

(def auth-url
  (str (url "/authorize"
            {:response_type "code"
             :client_id     client-id
             :redirect_uri  redirect-uri
             ;; TODO: supply a `state` for CSRF protection
             })
       "&scope=" (string/join " " scope)))
