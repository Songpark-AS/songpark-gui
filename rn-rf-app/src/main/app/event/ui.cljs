(ns app.event.ui
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]))


(rf/reg-event-db
 :inc-counter
 (fn [db [_ _]]
   (update db :counter inc)))

(rf/reg-event-db
 :set-view
 (fn [db [_ view-name]]
   (assoc db :view view-name)))

(rf/reg-event-db
 :set-balance
 (fn [db [_ balance]]
   (assoc-in db [:studio :balance] balance)))

(rf/reg-event-db
 :set-balance-slider
 (fn [db [_ balance]]
   (prn (str "set-balance-slider event: " balance))
   (assoc-in db [:studio :balance-slider] balance)))

(rf/reg-event-db
 :set-balance-sliding
 (fn [db [_ is-sliding]]
   (assoc-in db [:studio :balance-sliding] is-sliding)))

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
 :teleporter/status
 (fn [{db :db} [_ data]]
   {:dispatch [:http/get "http://192.168.11.123:3000/api/client/connect" data :teleporter/response]}))


;; testing ground
(comment
  (rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])
  


  (log/info @re-frame.db/app-db)

  )
