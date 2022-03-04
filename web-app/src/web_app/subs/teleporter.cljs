(ns web-app.subs.teleporter
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :teleporters
 (fn [db _]
   (->> db
        :teleporters
        (vals)
        (sort-by :teleporter/nickname))))

(rf/reg-sub
 :teleporter/online?
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/online?])))

(rf/reg-sub
 :teleporter/upgrading?
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/upgrading?])))

(rf/reg-sub
 :teleporter/net-config
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/net-config])))

(rf/reg-sub
 :teleporter/coredump
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/coredump])))

(rf/reg-sub
 :teleporter/apt-version
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/apt-version])))

(rf/reg-sub
 :teleporter/latest-available-apt-version
 (fn [db _]
   (get db :teleporter/latest-available-apt-version)))

(rf/reg-sub
 :teleporter/upgrade-status
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/upgrade-status])))


(rf/reg-sub
 :teleporter/offline-timeout
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/offline-timeout])))

(rf/reg-sub
 :teleporter/upgrade-timeout
 (fn [db [_ tp-id]]
   (get-in db [:teleporters tp-id :teleporter/upgrade-timeout])))

(rf/reg-sub
 :teleporter/jam-status
 (fn [db [_ tp-id]]
   (let [status (get-in db [:teleporters tp-id :jam/status] :idle)
         sip (get-in db [:teleporters tp-id :jam/sip])
         stream (get-in db [:teleporters tp-id :jam/stream])
         sync (get-in db [:teleporters tp-id :jam/sync])]
     {:jam/status status
      :jam/sip sip
      :jam/stream stream
      :jam/sync sync})))
