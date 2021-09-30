(ns web-app.message.handler.outgoing
  (:require [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]))

(defmulti outgoing :message/type)

(defmethod outgoing :app.cmd/subscribe [{:message/keys [topics]
                                              :keys [mqtt]}]
  (log/debug :outgoing (str "Subscribing to " topics))
  (protocol.mqtt.manager/subscribe mqtt topics))


(defmethod outgoing :teleporter.cmd/set-volume [{:message/keys [topic body]
                                                 :keys [mqtt]}]
  (protocol.mqtt.manager/publish mqtt (str topic) body))

(defmethod outgoing :teleporter.msg/info [{:message/keys [topic body]
                                           :keys [mqtt]}]
  (.publish mqtt topic body))

(defmethod outgoing :default [{:message/keys [type] :as message}]
  (throw
   (ex-info (str "No message handler defined for message type " type) message)))
