(ns web-app.event.ui
  (:require ["antd" :refer [message notification]]
            [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [taoensso.timbre :as log]
            [web-app.api :refer [send-message!]]
            [web-app.data :as data]
            [web-app.utils :refer [get-api-url get-platform-url]]))

(rf/reg-event-db
 :set-tp-list-selection-mode
 (fn [db [_ value]]
   (assoc db :tp-list-selection-mode value)))

(rf/reg-event-db
 :set-selected-teleporters
 (fn [db [_ teleporters]]
   (assoc db :selected-teleporters teleporters)))

(rf/reg-event-db
 :set-selected-teleporters-staging
 (fn [db [_ teleporters]]
   (assoc db :selected-teleporters-staging teleporters)))


(rf/reg-event-fx
 :save-ipv6
 (fn [_ [_ topic values]]
   (send-message! {:message/type :teleporter.cmd/save-ipv6
                   :message/topic topic
                   :message/body {:message/type :teleporter.msg/info
                                  :values values}})))

;; testing ground
(comment
  (rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])
  

  (rf/reg-event-fx
   :teleporter/status
   (fn [{db :db} [_ data]]
     {:dispatch [:http/get "http://127.0.0.1:3000/api/client/connect" data :teleporter/response]}))

  (log/info @re-frame.db/app-db)

  )


(comment
  (rf/dispatch [:fetch-teleporters])
  "e2f7ce6a-54fb-5704-b903-d7a772fce86e"
  )

