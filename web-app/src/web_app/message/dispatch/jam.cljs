(ns web-app.message.dispatch.jam
  (:require [re-frame.core :as rf]
            [songpark.common.protocol.mqtt.manager :as mqtt]
            [taoensso.timbre :as log]
            [web-app.data :as data]
            [web-app.message.dispatch.interface :as message]))


(defmethod message/dispatch :jam.cmd/ended [{:keys [mqtt-manager
                                                    message/body]}]
  (log/debug ::unsubscribe (str "Unsubscribing: " (data/get-jam)))
  (mqtt/unsubscribe mqtt-manager [(data/get-jam)])
  (data/clear-jam-id!))
