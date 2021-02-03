(ns hub.spotify.util
  (:require
   [clj-http.client :as http]
   [clojure.data.json :as json]
   [hub.spotify.auth :as auth]
   [hub.spotify.token :as token]
   [ring.util.codec :as codec]))

(defn url
  ([target endpoint]
   (str "https://" (name target) ".spotify.com" endpoint))
  ([target endpoint query-params]
   (url target (str endpoint "?" (codec/form-encode query-params)))))

(def api (partial url :api))

(defn parse-json [s]
  (json/read-str s :key-fn keyword))

(defn request!
  ([req]
   (request! req (or (token/api-token) (auth/client-credentials))))
  ([req bearer-token]
   (letfn [(send-request! [req]
             (auth/with-refresh-token
               (http/request req)))]
     (-> req
         (assoc :oauth-token (:access_token bearer-token))
         send-request!
         (update :body parse-json)))))

(defn get! [url]
  (:body (request! {:method :get :url url})))

(defn results
  "Extract the results vector from the nested body.
  e.g. {:albums [...]} => [...]
  Has to work for any key.."
  [body]
  (if (:items body)
    body
    (first (vals body))))

(defn crawl! [url]
  (loop [acc [] url url]
    (let [{:keys [items next]} (results (get! url))]
      (if next
        (recur (concat acc items) next)
        (concat acc items)))))
