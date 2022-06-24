(ns web-app.subs.teleporter
  (:require [re-frame.core :as rf]
            [web-app.subs.util :refer [get-selected-teleporter]]))

(rf/reg-sub
 :teleporters
 (fn [db _]
   (->> db
        :teleporters
        (vals)
        (sort-by :teleporter/nickname))))

(rf/reg-sub
 :teleporter/paired?
 (fn [db _]
   (->> db
        :teleporters
        first
        some?)))

(defn- get-tp-id [db tp-id]
  (if (nil? tp-id)
    (->> db
         :teleporters
         (vals)
         first
         :teleporter/id)
    tp-id))

(rf/reg-sub
 :teleporter/teleporter
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id]))))

(rf/reg-sub
 :teleporter/online?
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/online?]))))

(rf/reg-sub
 :teleporter/upgrading?
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/upgrading?]))))

(rf/reg-sub
 :teleporter/net-config
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/net-config]))))

(rf/reg-sub
 :teleporter/coredump
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :jam/coredump]))))

(rf/reg-sub
 :teleporter/apt-version
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/apt-version]))))

(rf/reg-sub
 :teleporter/latest-available-apt-version
 (fn [db _]
   (get db :teleporter/latest-available-apt-version)))

(rf/reg-sub
 :teleporter/upgrade-status
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/upgrade-status]))))


(rf/reg-sub
 :teleporter/offline-timeout
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/offline-timeout]))))

(rf/reg-sub
 :teleporter/upgrade-timeout
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/upgrade-timeout]))))

(rf/reg-sub
 :teleporter/jam-status
 (fn [db [_ tp-id]]
   (let [tp-id (if (nil? tp-id)
                 (:teleporter/id (get-selected-teleporter db))
                 tp-id)
         status (get-in db [:teleporters tp-id :jam/status] :idle)
         sip (get-in db [:teleporters tp-id :jam/sip])
         stream (get-in db [:teleporters tp-id :jam/stream])
         sync (get-in db [:teleporters tp-id :jam/sync])
         out {:jam/status status
              :jam/sip sip
              :jam/stream stream
              :jam/sync sync}
         with-tp-id (if-not (#{:idle :jam/waiting} status)
                      (reduce (fn [out [_ {:keys [jam/members]}]]
                                (let [m (set members)]
                                 (if (m tp-id)
                                   (reduced (-> m (disj tp-id) first))
                                   false)))
                              false (:jams db))
                      nil)]
     (assoc out
            :jam/with (get-in db [:teleporters with-tp-id :teleporter/nickname])))))


(rf/reg-sub
 :teleporter/setting
 (fn [db [_ tp-id tp-settings-k]]
   (let [tp-id (get-tp-id db tp-id)]
     (or (get-in db [:teleporters tp-id :teleporter/setting tp-settings-k])
         (get-in db [:teleporters tp-id tp-settings-k])))))
