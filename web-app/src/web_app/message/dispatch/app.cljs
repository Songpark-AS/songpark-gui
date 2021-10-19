(ns web-app.message.dispatch.app
  (:require [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [web-app.data :as data]
            [web-app.message.dispatch.interface :as message]))


;; topic is jam-id
(defmethod message/dispatch :app.cmd/subscribe [{:message/keys [topic]
                                                 :keys [mqtt-manager]}]
  (data/set-jam-id! topic)
  (let [topic (data/get-jam)]
    (log/debug ::subscribe (str "Subscribing to " topic))
    (protocol.mqtt.manager/subscribe mqtt-manager [topic])))

(defmethod message/dispatch :app.cmd/unsubscribe [{:message/keys [topic]
                                                   :keys [mqtt-manager]}]
  (let [topic (data/get-jam)]
    (log/debug ::unsubscribe (str "Unsubscribing: " topic))
    (protocol.mqtt.manager/unsubscribe mqtt-manager [topic])
    (data/clear-jam-id!)))

