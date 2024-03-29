(ns hub.spotify.auth
  "https://developer.spotify.com/documentation/general/guides/authorization-guide/"
  (:require
   [clj-http.client :as http]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as string]
   [hiccup.page :refer [html5]]
   [hub.spotify.token :as token]
   [hub.util :refer [parse-json]]
   [hub.util.webserver :as webserver]
   [ring.util.codec :as codec]))

;; TODO: from more secure location than local envvars
(def client-secret (System/getenv "SPOTIFY_SECRET"))
(def client-id (System/getenv "SPOTIFY_ID"))

(def scope
  ["user-library-read"
   "user-top-read"
   "playlist-read-private"
   "playlist-read-collaborative"
   "playlist-modify-public"
   "playlist-modify-private"])

(defn post-basic-auth [form-params]
  (:body (http/post "https://accounts.spotify.com/api/token"
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
  {:pre [(:refresh_token api-token)]}
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

(def ^:dynamic *PORT* 4000)
(defn redirect-uri []
  (str "http://127.0.0.1:" *PORT* "/en/spotify/callback"))

(defn authorize-handler [_]
  {:status  303
   :headers {"Content-Type" "text/html"
             "Location"     (str "https://accounts.spotify.com/authorize?"
                                 (codec/form-encode
                                  {:response_type "code"
                                   :client_id     client-id
                                   :redirect_uri  (redirect-uri)
                                   ;; TODO: supply a `state` for CSRF protection
                                   })
                                 "&scope=" (string/join " " scope))}})

(defn callback-handler [req]
  (token/persist!
   (post-basic-auth {:grant_type   "authorization_code"
                     :code         (get-in req [:params "code"])
                     :redirect_uri (redirect-uri)}))
  {:status 200
   :body   (html5 [:h1 "Authorized!"])})

(def routes
  ["/en/spotify"
   ["/authorize" {:handler #'authorize-handler}]
   ["/callback"  {:handler #'callback-handler}]])

(defn wait-for-auth
  "Poll+sleep until we see manual authentication token present.

  To get this token you have to start this server, open one of its endpoints in
  the browser, and go through the Spotify login process. This will just park
  until we see that value come through. Options for polling interval and
  timeout. If the timeout is exceeded, throw an exception.

  Both interval and timeout values are in seconds"
  [poll-interval timeout]
  (loop [seconds-slept 0]
    (cond
      (token/refresh-token?)
      nil

      (< timeout seconds-slept)
      (throw (ex-info "wait-for-auth timed out"
                      {:poll-interval poll-interval
                       :timeout  timeout}))

      :else (do (Thread/sleep (* 1000 poll-interval))
                (recur (+ seconds-slept poll-interval))))))

(defn manual-auth [port]
  (binding [*PORT* port]
    (when-not (token/refresh-token?)
      (let [server  (webserver/start! (webserver/minimal-setup routes) 4000)]
        (sh "open" (str "http://127.0.0.1:" *PORT* "/en/spotify/authorize"))
        (try
          (wait-for-auth 1000 300)
          (finally (webserver/stop! server)))))))
