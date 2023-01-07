(ns web-app.event.jam
  (:require [re-frame.core :as rf]
            [songpark.jam.util :refer [get-jam-subscription-topic]]
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
 :jam/started
 [mqtt-client]
 (fn [{:keys [db mqtt-client]} [_ {:keys [jam/id] :as msg}]]
   (let [topics (get-jam-subscription-topic id)]
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
   (let [topics (get-jam-subscription-topic id)]
     (mqtt/unsubscribe mqtt-client (keys topics)))
   (let [db (reduce (fn [db tp-id]
                      (-> db
                          (assoc-in [:teleporters tp-id :jam/status] :idle)
                          (assoc-in [:teleporters tp-id :jam/stream] nil)
                          (assoc-in [:teleporters tp-id :jam/sync] nil)
                          (assoc-in [:teleporters tp-id :jam/coredump] nil)))
                    (update-in db [:jams] dissoc id) members)]
     {:db db})))

(defn jam-teleporter-status [db [_ {:keys [teleporter/id event/type]}]]
  (update-in db [:teleporters id] merge (case type
                                          :sync/syncing {:jam/sync type}
                                          :sync/failed {:jam/sync type}
                                          :sync/synced {:jam/sync type}
                                          :sync/timeout {:jam/sync type}
                                          :sync/responded {:jam/sync type}
                                          :stream/stopped {:jam/stream type}
                                          :stream/streaming {:jam/stream type}
                                          :stream/broken {:jam/stream type})))

(rf/reg-event-db
 :jam.teleporter/status
 jam-teleporter-status)

(comment
  (jam-teleporter-status {:teleporters {:tp1 {} :tp2 {}}}
                         [:jam.teleporter/status {:jam/teleporters {:tp1 {:sip :sip/in-call :stream :streaming/streaming}}}])
  )
