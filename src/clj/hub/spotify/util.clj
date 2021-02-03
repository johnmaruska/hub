(ns hub.spotify.util
  (:require
   [clj-http.client :as http]
   [hub.spotify.auth :as auth]
   [clojure.data.json :as json]))

(defn parse-json [s]
  (json/read-str s :key-fn keyword))

(defn request!
  ([req]
   (request! req (or @auth/token (auth/client-credentials))))
  ([req bearer-token]
   (-> req
       (assoc :oauth-token (:access_token bearer-token))
       http/request
       (update :body parse-json))))

(defn get! [url]
  (request! {:method :get :url url}))

(defn results
  "Extract the results vector from the nested body.
  e.g. {:albums [...]} => [...]
  Has to work for any key.."
  [response]
  (first (vals (:body response))))

(defn crawl! [url]
  (loop [acc [] url url]
    (let [{:keys [items next]} (results (get! url))]
      (if next
        (recur (concat acc items) next)
        acc))))
