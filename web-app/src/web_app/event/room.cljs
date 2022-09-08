(ns web-app.event.room
  (:require [re-frame.core :as rf]
            [web-app.event.util :refer [message-base]]
            [web-app.utils :refer [get-api-url get-platform-url]]
            [taoensso.timbre :as log]))


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
   (let [response (:response data)]
     (merge {:db (update db :room/jam merge response)
             :rfe/push-state :views.room/jam}
            (condp = (:error/key response)
              :room/already-hosted {:dispatch [:http/get
                                               (get-api-url "/room/jam")
                                               {}
                                               :app.init.room/jam]}
              nil)))))

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


(rf/reg-event-fx
 :room.jam/knocked
 (fn [{:keys [db]} [_ {:keys [room/jammer room/id] :as data}]]
   (let [jam-room (:room/jam db)]
     (log/debug data)
     (if (and jam-room
              (= (:room/id jam-room) id))
       (let [jammers (get-in db [:room/jam :room/jammers])
             new-jammers (assoc jammers (:auth.user/id jammer) jammer)]
         {:db (assoc-in db [:room/jam :room/jammers] new-jammers)})
       {:db db}))))

;; TODO: add :room.jam/leave

(rf/reg-event-db
 :room.jam/left
 (fn [db [_ {room-id :room/id user-id :auth.user/id}]]
   (let [jam-room (:room/jam db)]
     (if (and jam-room
              (= (:room/id jam-room) room-id))
       (update-in db [:room/jam :room/jammers] dissoc user-id)
       db))))

;; TODO: add :room.jam/accept and :room.jam/accepted
;; TODO: add :room.jam/decline :room.jam/declined
;; TODO: add :room.jam/remove :room.jam/removed
;; TODO: add :room.jam/close :room.jam/closed
