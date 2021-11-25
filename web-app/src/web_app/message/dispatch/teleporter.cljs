(ns web-app.message.dispatch.teleporter
  (:require [re-frame.core :as rf]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [taoensso.timbre :as log]
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


(defmethod message/dispatch :teleporters/listen [{:keys [mqtt-manager]
                                                  :message/keys [body]}]
  (doseq [id (map :teleporter/uuid body)]
    (let [topic (str id "/log")]
      (log/info "Subscribing to " topic)
      (protocol.mqtt.manager/subscribe mqtt-manager [topic]))))

(defmethod message/dispatch :teleporter/log [{:message/keys [body id]}]
  (rf/dispatch [:teleporter/log (assoc body :message/id id)]))

(defmethod message/dispatch :teleporters/listen-net-config-report [{:keys [mqtt-manager]
                                                                    :message/keys [body]}]
  (doseq [id (map :teleporter/uuid body)]
    (let [topic (str id "/net-config-report")]
      (log/info "Subscribing to " topic)
      (protocol.mqtt.manager/subscribe mqtt-manager [topic]))))

(defmethod message/dispatch :teleporter/net-config-report [{:message/keys [body id]}]
  (log/debug :net-config-report body)
  (rf/dispatch [:teleporter/net-config body]))
