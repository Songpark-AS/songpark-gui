(ns web-app.subs.ui
  (:require [re-frame.core :as rf]))

;; (rf/reg-sub
;;  :get-counter
;;  (fn [db _]
;;    (:counter db)))

;; (rf/reg-sub
;;  :view
;;  (fn [db _]
;;    (:view db)))

;; (rf/reg-sub
;;  :balance
;;  (fn [db _]
;;    (get-in db [:studio :audio/balance] 0)))

;; (rf/reg-sub
;;  :balance-slider
;;  (fn [db _]
;;    (get-in db [:studio :audio/balance-slider] 0)))

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
 :teleporters
 (fn [db _]
   (:teleporters db)))

(rf/reg-sub
 :teleporter/data
 (fn [db _]
   (:teleporter/data db)))

(rf/reg-sub
 :mqtt/data
 (fn [db _]
   (:mqtt/data db)))


(comment
  @(rf/subscribe [:data/msg])
  @(rf/subscribe [:teleporters])
  )
