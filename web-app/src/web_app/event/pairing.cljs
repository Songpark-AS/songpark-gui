(ns web-app.event.pairing
  (:require [re-frame.core :as rf]
            [songpark.mqtt :as mqtt]
            [songpark.mqtt.util :refer [heartbeat-topic
                                        broadcast-topic]]
            [web-app.mqtt.interceptor :refer [mqtt-client]]))


(rf/reg-event-fx
 :pairing/paired
 (fn [{:keys [db]} [_ data]]
   {:dispatch [:app.init/app data]
    :rfe/push-state :views.teleporter/paired}))

(rf/reg-event-fx
 :pairing/unpaired
 [mqtt-client]
 (fn [{:keys [db mqtt-client]} [_ data]]
   (let [tp-id (:teleporter/id data)
         ht (heartbeat-topic tp-id)
         bt (broadcast-topic tp-id)
         timeout-obj (get-in db [:teleporters tp-id :teleporter/offline-timeout])]
     (when timeout-obj
       (js/clearTimeout timeout-obj))
     (mqtt/unsubscribe mqtt-client [ht bt]))
   {:db (dissoc db :teleporters)}))
