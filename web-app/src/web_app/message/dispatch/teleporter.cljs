(ns web-app.message.dispatch.teleporter
  (:require [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [web-app.data :as data]
            [web-app.message.dispatch.interface :as message]))


(defn- select-message [data]
  (select-keys data [:message/body :message/type]))

(defmethod message/dispatch :teleporter.cmd/global-volume [{:keys [mqtt-manager]
                                                            :as data}]
  (protocol.mqtt.manager/publish mqtt-manager (data/get-jam-teleporters)
                                 (select-message data)))


(defmethod message/dispatch :teleporter.cmd/network-volume [{:keys [mqtt-manager]
                                                             :as data}]
  (protocol.mqtt.manager/publish mqtt-manager (data/get-jam-teleporters)
                                 (select-message data)))

(defmethod message/dispatch :teleporter.cmd/local-volume [{:keys [mqtt-manager]
                                                             :as data}]
  (protocol.mqtt.manager/publish mqtt-manager (data/get-jam-teleporters)
                                 (select-message data)))

(defmethod message/dispatch :teleporter.cmd/save-ipv4 [{:message/keys [topic body]
                                                        :keys [mqtt-manager]}]
  (log/debug ::save-ipv4 "body: " body)
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))

(defmethod message/dispatch :teleporter.msg/info [{:message/keys [topic body]
                                                   :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))
