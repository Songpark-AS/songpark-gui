(ns web-app.event.mqtt
  (:require [songpark.mqtt :as mqtt]
            [songpark.mqtt.util :refer [teleporter-topic]]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [web-app.event.util :refer [message-base]]
            [web-app.mqtt.interceptor :refer [mqtt-client]]
            [web-app.subs.util :refer [get-tp-id]]))

(rf/reg-event-fx
 :mqtt/subscribe
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ topic ?qos]]
   (try
     (if topic
       (mqtt/subscribe mqtt-client (str topic) (or ?qos 2)))
     (catch js/Error e
       (log/error ::mqtt-subscribe "Failed to subscribe" {:exception e
                                                          :topic topic
                                                          :?qos ?qos})))
   nil))

(rf/reg-event-fx
 :mqtt/unsubscribe
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ ?topic]]
   (try
     (if ?topic
       (mqtt/unsubscribe mqtt-client (str ?topic))
       (mqtt/unsubscribe mqtt-client (keys @(:topics mqtt-client))))
     (catch js/Error e
       (log/error ::mqtt-subscribe "Failed to unsubscribe" {:exception e
                                                            :?topic ?topic})))
   nil))

(rf/reg-event-fx
 :mqtt/publish-message
 [mqtt-client]
 (fn [{:keys [mqtt-client db]} [_ topic message]]
   (mqtt/publish mqtt-client
                 topic
                 (message-base
                  db
                  message))
   nil))

(rf/reg-event-fx
 :mqtt/send-message-to-teleporter
 [mqtt-client]
 (fn [{:keys [mqtt-client db]} [_ teleporter-id message]]
   (let [tp-id (get-tp-id db teleporter-id)]
     (mqtt/publish mqtt-client
                   (teleporter-topic tp-id)
                   (message-base
                    db
                    message)))
   nil))
