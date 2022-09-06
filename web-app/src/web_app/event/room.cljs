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
 :room.session/host
 (fn [_ [_ room-id]]
   {:dispatch [:http/post
               (get-api-url "/room/session/host")
               {:room/id room-id}
               :room.session/hosted
               :room.session/hosted-failed]}))

(rf/reg-event-fx
 :room.session/hosted
 (fn [{:keys [db]} [_ {:keys [room/id]}]]
   {:db (assoc db :room/session {:room/id id})
    :rfe/push-state :views.room/session}))

(rf/reg-event-fx
 :room.session/hosted-failed
 (fn [{:keys [db]} [_ data]]
   ;; data is an error message -> #{:error/key :error/message}
   #_{:db (update db :room/session merge data)
    :rfe/push-state :views.room/session}))
