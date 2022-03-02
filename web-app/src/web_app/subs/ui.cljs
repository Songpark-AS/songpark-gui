(ns web-app.subs.ui
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :platform-version
 (fn [db _]
   (:platform/version db)))

(rf/reg-sub
 :tp-list-selection-mode
 (fn [db _]
   (:tp-list-selection-mode db)))

(rf/reg-sub
 :selected-teleporters
 (fn [db _]
   (:selected-teleporters db)))

(rf/reg-sub
 :selected-teleporters-staging
 (fn [db _]
   (:selected-teleporters-staging db)))

(rf/reg-sub
 :jam/started?
 (fn [db _]
   (:jam/started? db)))

(rf/reg-sub
 :teleporter/data
 (fn [db _]
   (:teleporter/data db)))

(rf/reg-sub
 :mqtt/data
 (fn [db _]
   (:mqtt/data db)))


(rf/reg-sub
 :jam
 (fn [db _]
   (:jam db)))

(rf/reg-sub
 :teleporter/log
 (fn [db [_ teleporter-id level]]
   (get-in db [:teleporter/log (str teleporter-id) level])))

(rf/reg-sub
 :view.telemetry.log/teleporter
 (fn [db _]
   (get db :view.telemetry.log/teleporter)))

(rf/reg-sub
 :view.telemetry.log/level
 (fn [db _]
   (get db :view.telemetry.log/level)))

(rf/reg-sub
 :view.telemetry/tab
 (fn [db _]
   (get db :view.telemetry/tab "VERSIONS")))


(comment
  @(rf/subscribe [:data/msg])
  @(rf/subscribe [:teleporters])
  @(rf/subscribe [:teleporter/net-config #uuid "ad6fc5b7-c52c-5941-bfb7-cf4fb4189775"])

  

  )
