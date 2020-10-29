(ns hub.discljord.spec.event
  (:require
   [hub.discljord.spec.channels :as channels]
   [malli.core :as m]))

(def message-create?
  (m/validator channels/message))
