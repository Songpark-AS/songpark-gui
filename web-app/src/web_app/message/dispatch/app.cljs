(ns web-app.message.dispatch.app
  (:require [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [web-app.message.dispatch.interface :as message]))


(defmethod message/dispatch :app.cmd/subscribe [{:message/keys [topics]
                                         :keys [mqtt-manager]}]
  (log/debug ::subscribe (str "Subscribing to " topics))
  (protocol.mqtt.manager/subscribe mqtt-manager topics))

(defmethod message/dispatch :app.cmd/unsubscribe [{:message/keys [topics]
                                                   :keys [mqtt-manager]}]
  (log/debug ::unsubscribe (str "Unsubscribing: " topics))
  (protocol.mqtt.manager/unsubscribe mqtt-manager topics))
