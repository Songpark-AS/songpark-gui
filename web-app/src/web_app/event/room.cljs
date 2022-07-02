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
