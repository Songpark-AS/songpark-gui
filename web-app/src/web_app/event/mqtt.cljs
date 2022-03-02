(ns web-app.event.mqtt
  (:require [re-frame.core :as rf]
            [web-app.mqtt.interceptor :refer [mqtt-client]]))

(rf/reg-event-fx
 :mqtt/send-message
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ topic message]]
   (mqtt/client mqtt-client topic message)
   nil))
