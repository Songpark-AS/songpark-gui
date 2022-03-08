(ns web-app.message.dispatch.teleporter
  (:require [re-frame.core :as rf]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [taoensso.timbre :as log]
            [songpark.common.config :refer [config]]
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


(defmethod message/dispatch :teleporter.cmd/report-network-config [{:message/keys [topic]
                                                                    :keys [mqtt-manager]}]
  (log/debug ::report-network-config "Requesting network config from TP: " topic)
  (protocol.mqtt.manager/publish mqtt-manager topic {:message/type :teleporter.cmd/report-network-config}))

(defmethod message/dispatch :teleporter.cmd/upgrade [{:message/keys [topic body]
                                                      :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager topic {:message/type :teleporter.cmd/upgrade
                                                     :message/body body}))

(defmethod message/dispatch :teleporter.msg/info [{:message/keys [topic body]
                                                   :keys [mqtt-manager]}]
  (protocol.mqtt.manager/publish mqtt-manager (str topic) body))


(defmethod message/dispatch :teleporters/listen [{:keys [mqtt-manager]
                                                  :message/keys [body]}]
  (doseq [id (map :teleporter/uuid body)]
    (let [log-topic (str id "/log")
          heartbeat-topic (str id "/heartbeat")
          upgrade-status-topic (str id "/upgrade-status")
          apt-version-topic (str id "/apt-version")
          coredump-topic (str id "/coredump")]
      (log/info "Subscribing to " log-topic)
      (log/info "Subscribing to " heartbeat-topic)
      (log/info "Subscribing to " upgrade-status-topic)
      (log/info "Subscribing to " apt-version-topic)
      (log/info "Subscribing to " coredump-topic)
      (protocol.mqtt.manager/subscribe mqtt-manager [log-topic
                                                     heartbeat-topic
                                                     upgrade-status-topic
                                                     apt-version-topic
                                                     coredump-topic]))))

(defmethod message/dispatch :teleporter/log [{:message/keys [body id]}]
  (rf/dispatch [:teleporter/log (assoc body :message/id id)]))

(defmethod message/dispatch :teleporter/heartbeat [{:message/keys [body]}]
  (let [tp-id (:teleporter/id body)]
    (utils/register-tp-heartbeat tp-id (get-in @config [:heartbeat :timer]))))

(defmethod message/dispatch :teleporter/apt-version [{:message/keys [body]}]
  (log/debug :teleporter-apt-version body)
  (rf/dispatch [:teleporter/apt-version body]))

(defmethod message/dispatch :teleporters/listen-net-config-report [{:keys [mqtt-manager]
                                                                    :message/keys [body]}]
  (doseq [id (map :teleporter/uuid body)]
    (let [topic (str id "/net-config-report")]
      (log/info "Subscribing to " topic)
      (protocol.mqtt.manager/subscribe mqtt-manager [topic]))))

(defmethod message/dispatch :teleporter/net-config-report [{:message/keys [body id]}]
  (log/debug :net-config-report body)
  (rf/dispatch [:teleporter/net-config body]))

(defmethod message/dispatch :teleporter/coredump [{:message/keys [body id]}]
  (rf/dispatch [:teleporter/coredump body]))
(defmethod message/dispatch :teleporter/upgrade-status [{:message/keys [body]}]
  (rf/dispatch [:teleporter/upgrade-status body]))
