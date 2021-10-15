(ns web-app.message.dispatch.teleporter
  (:require [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [web-app.message.dispatch.interface :as message]))

(defmethod message/dispatch :teleporter.cmd.volume/global [{:message/keys [topic body]
                                                         :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))


(defmethod message/dispatch :teleporter.cmd/balance [{:message/keys [topic body]
                                                            :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))

(defmethod message/dispatch :teleporter.cmd/save-ipv4 [{:message/keys [topic body]
                                                        :keys [mqtt-manager]}]
  (log/debug ::save-ipv4 "body: " body)
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))

(defmethod message/dispatch :teleporter.msg/info [{:message/keys [topic body]
                                                   :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))
