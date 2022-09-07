(ns web-app.event.room
  (:require [re-frame.core :as rf]
            [web-app.event.util :refer [message-base]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :room/create
 (fn [_ [_ data handlers]]
   {:dispatch [:http/put
               (get-api-url "/room")
               data
               handlers]}))

(rf/reg-event-fx
 :room/jammer
 (fn [{:keys [db]} [_ auth-id k v]]
   (merge
    {:db (assoc-in db [:room/jammers auth-id k] v)}
    (when (= k :jammer/muted?)
      {:dispatch [:mqtt/send-message-to-teleporter
                  nil
                  (message-base
                   db
                   {:message/type :teleporter.cmd/network-mute
                    :teleporter/mute v})]}))))

(rf/reg-event-fx
 :room.jam/host
 (fn [_ [_ room-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/host")
               {:room/id room-id}
               :room.jam/hosted
               :room.jam/hosted-failed]}))

(rf/reg-event-fx
 :room.jam/hosted
 (fn [{:keys [db]} [_ data]]
   {:db (assoc db :room/jam data)
    :rfe/push-state :views.room/jam}))

(rf/reg-event-fx
 :room.jam/hosted-failed
 (fn [{:keys [db]} [_ data]]
   ;; data is an error message -> #{:error/key :error/message}
   {:db (update db :room/jam merge (:response data))
    :rfe/push-state :views.room/jam}))


(rf/reg-event-fx
 :room.jam/close
 (fn [_ [_ room-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/close")
               {:room/id room-id}
               :room.jam/closed
               :room.jam/closed-failed]}))

(rf/reg-event-fx
 :room.jam/closed
 (fn [{:keys [db]} [_ data]]
   {:db (dissoc db :room/jam)
    :rfe/push-state :views/room}))

(rf/reg-event-fx
 :room.jam/closed-failed
 (fn [{:keys [db]} [_ data]]
   ;; data is an error message -> #{:error/key :error/message}
   {:db (update db :room/jam merge (:response data))}))


(rf/reg-event-fx
 :room.jam/leave
 (fn [_ [_ room-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/leave")
               {:room/id room-id}
               :room.jam/left
               :room.jam/left-failed]}))

(rf/reg-event-fx
 :room.jam/left
 (fn [{:keys [db]} [_ data]]
   {:db (dissoc db :room/jam)
    :rfe/push-state :views/room}))

(rf/reg-event-fx
 :room.jam/left-failed
 (fn [{:keys [db]} [_ data]]
   ;; data is an error message -> #{:error/key :error/message}
   {:db (update db :room/jam merge (:response data))}))
