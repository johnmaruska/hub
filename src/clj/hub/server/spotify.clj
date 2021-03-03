(ns hub.server.spotify
  (:require
   [hiccup.page :refer [html5]]
   [hub.spotify.auth :as auth]))

(defn auth-redirect [_]
  {:status  303
   :headers {"Content-Type" "text/html"
             "Location" auth/auth-url}})

(defn auth-callback [req]
  (auth/authorization-code (get-in req [:params "code"]))
  {:status 200 :body (html5 [:h1 "Authorized!"])})

(def routes
  ["/en/spotify"
   ["/authorize" {:handler #'auth-redirect}]
   ["/callback"  {:handler #'auth-callback}]])
