(ns web-app.message.dispatch.teleporter
  (:require [re-frame.core :as rf]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [taoensso.timbre :as log]
            [web-app.data :as data]
            [web-app.utils :as utils]
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
    (let [log-topic (str id "/log")
          heartbeat-topic (str id "/heartbeat")]
      (log/info "Subscribing to " log-topic)
      (log/info "Subscribing to " heartbeat-topic)
      (protocol.mqtt.manager/subscribe mqtt-manager [log-topic heartbeat-topic]))))


(defmethod message/dispatch :teleporter/log [{:message/keys [body id]}]
  (rf/dispatch [:teleporter/log (assoc body :message/id id)]))

(defmethod message/dispatch :teleporter/heartbeat [{:message/keys [body]}]
  (let [tp-id (:teleporter/id body)]
    (utils/register-tp-heartbeat tp-id 6000)))
