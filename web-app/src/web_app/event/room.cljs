(ns web-app.event.room
  (:require [re-frame.core :as rf]
            [web-app.event.util :refer [message-base]]
            [web-app.utils :refer [get-api-url get-platform-url]]
            [web-app.subs.util :refer [get-tp-id]]
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
 :room.jam/history
 (fn [_ _]
   {:dispatch [:http/get
               (get-api-url "/room/jam/history")
               nil
               :room.jam.history/ok]}))

(rf/reg-event-db
 :room.jam.history/ok
 (fn [db [_ data]]
   (assoc db :room.jam/history data)))

(defn- update-cofx-from-error-message
  ([cofx data]
   (update-cofx-from-error-message cofx data nil))
  ([cofx {:error/keys [key] :as data} context]
   (case key
     :room/does-not-exist (-> cofx
                              (update-in [:db :room/jam] dissoc)
                              (assoc :rfe/push-state :views/room))
     :room/user-not-in-the-room (let [user-id (:auth.user/id context)]
                                  (cond
                                    user-id
                                    (update-in cofx [:db :room/jam :room/jammers] dissoc user-id)

                                    (true? (get-in cofx [:db :room/jam :room/knocking?]))
                                    (-> cofx
                                        (update-in [:db] dissoc :room/jam)
                                        (assoc :rfe/push-state :views/room))

                                    :else
                                    cofx))
     (update-in cofx [:db :room/jam] merge data))))

;; jam host

(rf/reg-event-fx
 :room.jam/host
 (fn [{:keys [db]} [_ room-id]]
   (let [tp-id (get-tp-id db nil)]
     {:db (update-in db [:teleporters tp-id] dissoc
                     :jam/status
                     :jam/sip
                     :jam/stream
                     :jam/sync)
      :dispatch [:http/post
                 (get-api-url "/room/jam/host")
                 {:room/id room-id}
                 :room.jam/hosted
                 :room.jam/hosted-failed]})))

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
;; eof jam host

;; jam close
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
 :room.jam.mqtt/closed
 (fn [{:keys [db]} [_ data]]
   {:db (dissoc db :room/jam)
    :rfe/push-state :views/room}))

(rf/reg-event-fx
 :room.jam/closed-failed
 (fn [cofx [_ data]]
   (let [response (:response data)]
     (update-cofx-from-error-message cofx response))))

;; eof jam close

;; jam leave
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
 (fn [cofx [_ data]]
   (let [response (:response data)]
    (update-cofx-from-error-message cofx response))))

(rf/reg-event-db
 :room.jam.mqtt/left
 (fn [db [_ {room-id :room/id user-id :auth.user/id}]]
   (let [jam-room (:room/jam db)]
     (if (and jam-room
              (= (:room/id jam-room) room-id))
       (update-in db [:room/jam :room/jammers] dissoc user-id)
       db))))
;; eof jam leave

;; jam knock
(rf/reg-event-fx
 :room.jam/knock
 (fn [_ [_ room-name handlers]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/knock")
               {:room/name room-name}
               handlers]}))

(rf/reg-event-fx
 :room.jam/knocked
 (fn [{:keys [db]} [_ data]]
   {:db (assoc db :room/jam (assoc data :room/knocking? true))
    :rfe/push-state :views.room/jam}))

(rf/reg-event-fx
 :room.jam.mqtt/knocked
 (fn [{:keys [db]} [_ {:keys [room/jammer room/id] :as data}]]
   (let [jam-room (:room/jam db)]
     (if (and jam-room
              (= (:room/id jam-room) id))
       (let [jammers (get-in db [:room/jam :room/jammers])
             new-jammers (assoc jammers (:auth.user/id jammer) jammer)]
         {:db (assoc-in db [:room/jam :room/jammers] new-jammers)})
       {:db db}))))
;; eof jam knock

;; jam decline
(rf/reg-event-fx
 :room.jam/decline
 (fn [_ [_ room-id user-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/decline")
               {:room/id room-id
                :auth.user/id user-id}
               :room.jam/declined
               :room.jam/declined-failed]}))
(rf/reg-event-db
 :room.jam/declined
 (fn [db [_ data]]
   (update-in db [:room/jam :room/jammers] dissoc (:auth.user/id data))))
(rf/reg-event-fx
 :room.jam/declined-failed
 (fn [cofx [_ data]]
   (let [response (:response data)]
     (update-cofx-from-error-message cofx response))))
(rf/reg-event-db
 :room.jam.mqtt/declined
 (fn [db [_ data]]
   (dissoc db :room/jam)))
;; eof jam decline

;; jam accept
(rf/reg-event-fx
 :room.jam/accept
 (fn [_ [_ room-id user-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/accept")
               {:room/id room-id
                :auth.user/id user-id}
               :room.jam/accepted
               :room.jam/accepted-failed]}))
(rf/reg-event-db
 :room.jam/accepted
 (fn [db [_ data]]
   (assoc-in db [:room/jam :room/jammers (:auth.user/id data) :jammer/status] :jamming)))
(rf/reg-event-fx
 :room.jam/accepted-failed
 (fn [cofx [_ data]]
   (let [response (:response data)]
     (update-cofx-from-error-message cofx response))))

(rf/reg-event-db
 :room.jam.mqtt/accepted
 (fn [db [_ data]]
   (assoc db :room/jam (:room/jam data))))
;; eof jam accept

;; jam remove
(rf/reg-event-fx
 :room.jam/remove
 (fn [_ [_ room-id user-id]]
   {:dispatch [:http/post
               (get-api-url "/room/jam/remove")
               {:room/id room-id
                :auth.user/id user-id}
               :room.jam/removed
               :room.jam/removed-failed
               {:auth.user/id user-id}]}))
(rf/reg-event-db
 :room.jam/removed
 (fn [db [_ data]]
   (update-in db [:room/jam :room/jammers] dissoc (:auth.user/id data))))
(rf/reg-event-fx
 :room.jam/removed-failed
 (fn [cofx [_ data ?context]]
   (let [response (:response data)]
     (update-cofx-from-error-message cofx response ?context))))

(rf/reg-event-db
 :room.jam.mqtt/removed
 (fn [db [_ data]]
   (assoc db :room/jam (:room/jam data))))
;; eof jam remove
