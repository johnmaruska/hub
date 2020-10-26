(ns hub.discljord.spec.primitives)

(def unix-timestamp int?)

(def int-string
  [:and string? [:re #"\d*"]])

(def discriminator
  [:and string? [:re #"\d{4}"]])

(def hex-string
  [:and string? [:re #"[a-zA-Z\d]*"]])

(def snowflake
  [:and string? [:re #"\d{18}"]])

(def iso8601 #"/(\d{4})-(\d{2})-(\d{2})T(\d{2})\:(\d{2})\:(\d{2})[+-](\d{2})\:(\d{2})/")

(def timestamp
  [:and string? [:re iso8601]])
