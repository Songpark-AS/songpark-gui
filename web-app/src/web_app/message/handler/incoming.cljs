(ns web-app.message.handler.incoming
  (:require [taoensso.timbre :as log]))

(defmulti incoming :message/type)
(defmethod incoming :teleporter.msg/info [{:message/keys [topic body]
                                           #_#_:keys [message-service mqtt]}]
  (log/debug ::incoming topic body))

(comment
  (incoming {:message/type :teleporter.msg/info
             :message/topic "World"
             :message/body "Hello"})
  )
