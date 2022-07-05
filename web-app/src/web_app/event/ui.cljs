(ns web-app.event.ui
  (:require ["antd" :refer [message notification]]
            [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [taoensso.timbre :as log]
            [web-app.data :as data]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-db
 :teleporter.view/select-teleporter
 (fn [db [_ tp-id]]
   (assoc db :teleporter.view/selected-teleporter tp-id)))

(rf/reg-event-db
 :component/radio-group
 (fn [db [_ group-key value]]
   (assoc-in db [:component/radio-group group-key] value)))

(rf/reg-event-db
 :component/clear!
 (fn [db _]
   (dissoc db :component/radio-group)))

;; testing ground
(comment
  (rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])

  (rf/reg-event-fx
   :teleporter/status
   (fn [{db :db} [_ data]]
     {:dispatch [:http/get "http://127.0.0.1:3000/api/client/connect" data :teleporter/response]}))

  (log/info @re-frame.db/app-db)

  )


(rf/reg-event-db
 :ui.fx.input/tab
 (fn [db [_ input tab]]
   (assoc-in db [:ui.fx.input/tab input] tab)))

(comment
  (rf/dispatch [:fetch-teleporters])
  "e2f7ce6a-54fb-5704-b903-d7a772fce86e"
  )
