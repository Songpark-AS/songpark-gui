(ns web-app.event.jam
  (:require [re-frame.core :as rf]
            [songpark.jam.util :refer [get-jam-topic-subscriptions]]
            [songpark.mqtt :as mqtt]
            [taoensso.timbre :as log]
            [web-app.data :as data]
            [web-app.mqtt.interceptor :refer [mqtt-client]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :jam/stop
 (fn [_ [_ jam-id]]
   {:dispatch [:http/delete (get-api-url "/jam") {:jam/id jam-id}]}))


(rf/reg-event-fx
 :jam/stop-by-teleporter-id
 (fn [{:keys [db]} [_ teleporter-id]]
   (when-let [[jam-id {:keys [jam/members]}] (->> db
                                                  :jams
                                                  (filter (fn [[k {:keys [jam/members]}]]
                                                            ((set members) teleporter-id)))
                                                  first)]
     (let [updated-db (reduce (fn [out tp-id]
                                (assoc-in out [:teleporters tp-id :jam/status] :stopping))
                              db members)]
       {:db updated-db
        :dispatch [:http/delete (get-api-url "/jam") {:jam/id jam-id}]}))))

(rf/reg-event-fx
 :jam/ask
 (fn [_ [_ tp-id]]
   {:dispatch [:http/put (get-api-url "/jam/ask") {:teleporter/id tp-id}]}))

(rf/reg-event-fx
 :jam/obviate
 (fn [_ [_ tp-id]]
   {:dispatch [:http/delete (get-api-url "/jam/ask") {:teleporter/id tp-id}]}))

(rf/reg-event-fx
 :jam/started
 [mqtt-client]
 (fn [{:keys [db mqtt-client]} [_ {:keys [jam/id] :as msg}]]
   (let [topics (get-jam-topic-subscriptions :app msg)]
     (mqtt/subscribe mqtt-client topics))
   (let [members (:jam/members msg)
         db (assoc-in db [:jams id] (select-keys msg [:jam/id :jam/members]))]
     {:db (reduce (fn [db tp-id]
                    (assoc-in db [:teleporters tp-id :jam/status] :jamming))
                  db members)})))


(rf/reg-event-fx
 :jam/stopped
 [mqtt-client]
 (fn [{:keys [db mqtt-client]} [_ {:keys [jam/id jam/members] :as msg}]]
   (let [topics (get-jam-topic-subscriptions :app msg)]
     (mqtt/unsubscribe mqtt-client (keys topics)))
   (let [db (reduce (fn [db tp-id]
                      (-> db
                          (assoc-in [:teleporters tp-id :jam/status] :idle)
                          (assoc-in [:teleporters tp-id :jam/sip] nil)
                          (assoc-in [:teleporters tp-id :jam/stream] nil)
                          (assoc-in [:teleporters tp-id :jam/sync] nil)
                          (assoc-in [:teleproters tp-id :jam/coredump] nil)))
                    (update-in db [:jams] dissoc id) members)]
     {:db db})))

(defn jam-teleporter-status [db [_ {:keys [teleporter/id jam/teleporters]}]]
  (reduce (fn [db [tp-id {:keys [sip stream sync]}]]
            (update-in db [:teleporters id] merge {:jam/sip sip :jam/stream stream :jam/sync sync}))
          db teleporters))

(rf/reg-event-db
 :jam.teleporter/status
 jam-teleporter-status)

(comment
  (jam-teleporter-status {:teleporters {:tp1 {} :tp2 {}}}
                         [:jam.teleporter/status {:jam/teleporters {:tp1 {:sip :sip/in-call :stream :streaming/streaming}}}])
  )
