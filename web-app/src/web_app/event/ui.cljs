(ns web-app.event.ui
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [web-app.mqtt :as mqtt]))


;; (rf/reg-event-db
;;  :inc-counter
;;  (fn [db [_ _]]
;;    (update db :counter inc)))

;; (rf/reg-event-db
;;  :set-view
;;  (fn [db [_ view-name]]
;;    (assoc db :view view-name)))

;; (rf/reg-event-db
;;  :set-balance
;;  (fn [db [_ balance]]
;;    (assoc-in db [:studio :balance] balance)))

;; (rf/reg-event-db
;;  :set-balance-slider
;;  (fn [db [_ balance]]
;;    (prn (str "set-balance-slider event: " balance))
;;    (assoc-in db [:studio :balance-slider] balance)))

;; (rf/reg-event-db
;;  :set-balance-sliding
;;  (fn [db [_ is-sliding]]
;;    (assoc-in db [:studio :balance-sliding] is-sliding)))

(rf/reg-event-db
 :set-tp-list-selection-mode
 (fn [db [_ value]]
   (assoc db :tp-list-selection-mode value)))

(rf/reg-event-db
 :set-teleporters
 (fn [db [_ teleporters]]
   (assoc db :teleporters teleporters)))

(rf/reg-event-db
 :set-selected-teleporters
 (fn [db [_ teleporters]]
   (assoc db :selected-teleporters teleporters)))

(rf/reg-event-db
 :set-selected-teleporters-staging
 (fn [db [_ teleporters]]
   (assoc db :selected-teleporters-staging teleporters)))

(rf/reg-event-db
 :set-jam
 (fn [db [_ jam]]
   (assoc db :jam jam)))

(rf/reg-event-db
 :teleporter/response
 (fn [db [_ {:keys [teleporter/bits teleporter/uuid teleporter/nickname
                    mqtt/username mqtt/password] :as response}]]
   (assoc db
          :loading? false
          :teleporter/data {:teleporter/nickname nickname
                            :teleporter/bits bits
                            :teleporter/uuid uuid}
          :mqtt/data {:mqtt/username username
                      :mqtt/password password})))

;; TODO: get config vars from a config.edn for this

(rf/reg-event-fx
 :fetch-teleporters
 (fn [_ _]
   {:dispatch [:http/get "http://127.0.0.1:3000/api/app" nil :set-teleporters]}))

(rf/reg-event-fx
 :start-session
 (fn [_ [_ uuids]]
   {:dispatch [:http/put "http://127.0.0.1:3000/api/jam" uuids :set-jam]}))





;; testing ground
(comment
  (rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])
  

  (rf/reg-event-fx
   :teleporter/status
   (fn [{db :db} [_ data]]
     {:dispatch [:http/get "http://192.168.11.123:3000/api/client/connect" data :teleporter/response]}))

  (log/info @re-frame.db/app-db)

  )


(comment
  (rf/dispatch [:fetch-teleporters])
  "e2f7ce6a-54fb-5704-b903-d7a772fce86e"
  )
