(ns hub.spotify.util
  "Utilities for requesting from the Spotify API."
  (:require
   [again.core :as again]
   [clj-http.client :as http]
   [hub.spotify.auth :as auth]
   [hub.spotify.token :as token]
   [hub.util :refer [parse-json]]
   [clojure.tools.logging :as log]))

(def api (partial str "https://api.spotify.com"))

(defn rate-limit
  "Delays thread by amount specified in HTTP response exception."
  [exc]
  (let [retry-after (get-in (ex-data exc) [:headers "retry-after"])
        delay-ms    (* 1000 (Integer/parseInt retry-after))]
    (Thread/sleep delay-ms)))

(defmacro with-rate-limiting
  "Retry 429 responses as specified in the HTTP response exception."
  [& body]
  `(again/with-retries
     {::again/callback (fn [s#]
                         (let [exc# (::again/exception s#)]
                           (if (= 429 (:status (ex-data exc#)))
                             (rate-limit exc#)
                             ::again/fail)))
      ::again/strategy (repeat 0)}
     ~@body))

(defn send-request!
  "Sends a request with all overhead managed, e.g. rate limiting."
  [req]
  (with-rate-limiting
    (auth/with-refresh-token
      (log/debug "Sending request" req)
      (http/request req))))

(defn request!
  ([req]
   (request! req (or (token/api-token) (auth/client-credentials))))
  ([req bearer-token]
   (-> req
       (assoc :oauth-token (:access_token bearer-token))
       send-request!
       (update :body #(when %1 (parse-json %1))))))

(defn delete! [url opts]
  (request! (merge opts {:method :delete :url url})))

(defn get!
  ([url] (get! url {}))
  ([url opts]
   (:body (request! (merge opts {:method :get :url url})))))

(defn post! [url opts]
  (request! (merge opts {:method :post :url url})))

(defn results
  "Extract the results vector from the nested body.
  e.g. {:albums [...]} => [...]
  Has to work for any key.."
  [body]
  (if (:items body)
    body
    (first (vals body))))

(defn crawl!
  "Crawl a paginated GET request, building lazyseq of results.
  Query parameters must be encoded into the url"
  ([url]
   (crawl! url {}))
  ([url opts]
   (loop [acc [] url url]
     (let [{:keys [items next]} (results (get! url opts))]
       (if next
         (recur (concat acc items) next)
         (concat acc items))))))
