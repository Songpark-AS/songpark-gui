(ns web-app.event.mqtt
  (:require [songpark.mqtt :as mqtt]
            [songpark.mqtt.util :refer [teleporter-topic]]
            [re-frame.core :as rf]
            [web-app.mqtt.interceptor :refer [mqtt-client]]))

(rf/reg-event-fx
 :mqtt/publish-message
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ topic message]]
   (mqtt/publish mqtt-client topic message)
   nil))

(rf/reg-event-fx
 :mqtt/send-message-to-teleporter
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ teleporter-id message]]
   (mqtt/publish mqtt-client (teleporter-topic teleporter-id) message)
   nil))
