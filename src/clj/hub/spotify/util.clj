(ns hub.spotify.util
  (:require
   [clj-http.client :as http]
   [hub.spotify.auth :as auth]
   [hub.spotify.token :as token]
   [hub.util :refer [parse-json]]
   [ring.util.codec :as codec]))

(def api (partial str "https://api.spotify.com"))

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
         (update :body #(when %1 (parse-json %1)))))))

(defn delete! [url opts]
  (request! (merge opts {:method :delete :url url})))

(defn get! [url]
  (:body (request! {:method :get :url url})))

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

(defn crawl! [url]
  (loop [acc [] url url]
    (let [{:keys [items next]} (results (get! url))]
      (if next
        (recur (concat acc items) next)
        (concat acc items)))))
