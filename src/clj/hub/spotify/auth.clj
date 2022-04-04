(ns hub.spotify.auth
  "https://developer.spotify.com/documentation/general/guides/authorization-guide/"
  (:require
   [clj-http.client :as http]
   [clojure.string :as string]
   [hiccup.page :refer [html5]]
   [hub.spotify.token :as token]
   [hub.util :refer [parse-json]]
   [ring.util.codec :as codec]))

;; TODO: from more secure location than local envvars
(def client-secret (System/getenv "SPOTIFY_SECRET"))
(def client-id (System/getenv "SPOTIFY_ID"))
;; TODO: pull from config depending on deploy location
;; TODO: pull path from a bidirectional library instead of hard code
(def redirect-uri "http://127.0.0.1:4000/en/spotify/callback")

(def scope
  ["user-library-read"
   "user-top-read"
   "playlist-read-private"
   "playlist-read-collaborative"
   "playlist-modify-public"
   "playlist-modify-private"])

(def url (partial str "https://accounts.spotify.com"))

(def auth-url
  (url "/authorize?"
       (codec/form-encode
        {:response_type "code"
         :client_id     client-id
         :redirect_uri  redirect-uri
         ;; TODO: supply a `state` for CSRF protection
         })
       "&scope=" (string/join " " scope)))

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

(defn unauthorized? [ex]
  (or (= "Unauthorized" (:reason-phrase (ex-data ex)))
      (= 401 (:status (ex-data ex)))))

(defn error-message [ex]
  (-> (ex-data ex) :body parse-json :error :message))

(defn expired-token? [ex]
  (try
    (= "The access token expired" (error-message ex))
    (catch NullPointerException _
      false)))

(defmacro with-refresh-token [& body]
  `(try
     ~@body
     (catch Exception ex#
       (if (and (unauthorized? ex#) (expired-token? ex#))
         (do
           (refresh-token! (token/api-token))
           ~@body)
         (throw ex#)))))

(defn authorization-code
  "Request an access token via the authorization code flow using the code
  provided by the /authorize endpoint."
  [auth-code]
  (token/persist!
   (post-basic-auth {:grant_type   "authorization_code"
                     :code         auth-code
                     :redirect_uri redirect-uri})))


;;; Manual Step

(defn authorize-handler [_]
  {:status  303
   :headers {"Content-Type" "text/html"
             "Location"     auth-url}})

(defn callback-handler [req]
  (authorization-code (get-in req [:params "code"]))
  {:status 200
   :body   (html5 [:h1 "Authorized!"])})

(def routes
  ["/en/spotify"
   ["/authorize" {:handler #'authorize-handler}]
   ["/callback"  {:handler #'callback-handler}]])
