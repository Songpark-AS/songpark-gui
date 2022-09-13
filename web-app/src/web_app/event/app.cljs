(ns web-app.event.app
  (:require [re-frame.core :as rf]
            [songpark.jam.util :refer [get-jam-topic-subscriptions]]
            [songpark.mqtt :as mqtt]
            [songpark.mqtt.util :refer [broadcast-topic
                                        heartbeat-topic
                                        teleporter-topic]]
            [taoensso.timbre :as log]
            [web-app.event.util :refer [add-error-message]]
            [web-app.mqtt.interceptor :refer [mqtt-client]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :app/init
 (fn [{:keys [db]} _]
   {:mqtt/start (:auth/user db)
    :fx [[:dispatch [:app/initialize true]]
         [:dispatch [:http/get
                     (get-api-url "/app")
                     {}
                     :app.init/app]]
         [:dispatch [:http/get
                     (get-api-url "/profile")
                     {}
                     :app.init/profile]]
         [:dispatch [:http/get
                     (get-api-url "/room")
                     {}
                     :app.init/rooms]]
         [:dispatch [:http/get
                     (get-api-url "/fx")
                     {}
                     :app.init.fx/presets]]
         [:dispatch [:http/get
                     (get-api-url "/room/jam")
                     {}
                     :app.init.room/jam]]]}))


(rf/reg-event-fx
 :app.init/app
 [mqtt-client]
 (fn [{:keys [db mqtt-client]} [_ {:keys [teleporter/teleporters
                                          jam/jams]}]]
   (doseq [{:keys [teleporter/id]} teleporters]
     (let [bt (broadcast-topic id)
           ht (heartbeat-topic id)]
       (try
         (mqtt/subscribe mqtt-client {bt 2
                                      ht 2})
         (catch js/Error e (log/error ::handle-init "failed to subscribe." e)))))
   (doseq [jam jams]
     (let [topics (get-jam-topic-subscriptions :app jam)]
       (try
         (mqtt/subscribe mqtt-client topics)
         (catch js/Error e (log/error ::handle-init "failed to subscribe." e)))))
   {:db (assoc db
               :teleporters (->> teleporters
                                 (map (juxt :teleporter/id identity))
                                 (into {}))
               :teleporter/id (->> teleporters
                                   first
                                   :teleporter/id)
               :jams (->> jams
                          (map (juxt :jam/id #(select-keys % [:jam/id :jam/members])))
                          (into {})))}))

(rf/reg-event-db
 :app.init/profile
 (fn [db [_ data]]
   (assoc db :profile/profile data)))

(rf/reg-event-db
 :app.init.fx/presets
 (fn [db [_ data]]
   (assoc db :fx/presets data)))


(rf/reg-event-db
 :app.init/rooms
 (fn [db [_ data]]
   (assoc db :room/room data)))

(rf/reg-event-db
 :app.init/room
 (fn [db [_ data]]
   (update db :room/room conj data)))

(rf/reg-event-db
 :app.init.room/jam
 (fn [db [_ data]]
   (if (= data {:result :success})
     db
     (assoc db :room/jam data))))


(rf/reg-event-db
 :app/initialize
 (fn [db [_ value]]
   (assoc db :app/initialized? value)))
