(ns web-app.event.ui
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [web-app.api :refer [send-message!]]
            [web-app.mqtt :as mqtt]
            ["/web_app/config" :as config]
            ))


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
(let [webapp-config (js->clj config :keywordize-keys true)
      platform-api-base-url (str (:host (:platform webapp-config))
                             ":"
                             (:port (:platform webapp-config))
                             (:api_base (:platform webapp-config)))]

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

  ;; (rf/reg-event-db
  ;;  :set-jam
  ;;  (fn [db [_ jam]]
  ;;    (assoc db :jam jam)))

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

  (rf/reg-event-db
   :jam/started?
   (fn [db [_ started?]]
     (assoc db
            :jam/started? started?)))

  ;; TODO: get config vars from a config.edn for this

  (rf/reg-event-fx
   :subscribe-jam
   (fn [_ [_ jam]]
     (let [uuid (:jam/uuid jam)]
       {:dispatch-n [[:mqtt/subscribe (str uuid)] [:jam/started? true]]})))

  (rf/reg-event-fx
   :set-jam
   (fn [cofx [_ jam]]
     {:db (assoc (:db cofx) :jam jam)
      :fx [[:dispatch [:subscribe-jam jam]]]}
     ))

  (rf/reg-event-fx
   :on-jam-deleted
   (fn [cofx [_ response]]
     (log/debug ::on-jam-deleted "response" response)
     (let [jam (rf/subscribe [:jam])]
       {:fx [
             [:dispatch [:mqtt/unsubscribe [(str (:jam/uuid @jam))]]]
             [:dispatch [:jam/started? false]]
             ]})))

  (rf/reg-event-fx
   :fetch-teleporters
   (fn [_ _]
     {:dispatch [:http/get (str platform-api-base-url "/app") nil :set-teleporters]}))

  (rf/reg-event-fx
   :start-jam
   (fn [_ [_ uuids]]
     {:dispatch [:http/put (str platform-api-base-url "/jam") uuids :set-jam]}))

  (rf/reg-event-fx
   :stop-jam
   (fn [_ [_ jam-uuid]]
     {:dispatch [:http/delete (str platform-api-base-url "/jam") {:jam/uuid jam-uuid} :on-jam-deleted]}))


  (rf/reg-event-fx
   :save-ipv4
   (fn [_ [_ topic values]]
     (send-message! {:message/type :teleporter.cmd/save-ipv4
                     :message/topic topic
                     :message/body {:message/type :teleporter.msg/ipv4
                                    :message/values values}})))

  (rf/reg-event-fx
   :mqtt/subscribe
   (fn [_ [_ topic]]
     (send-message! {:message/type :app.cmd/subscribe
                     :message/topic topic})))

  (rf/reg-event-fx
   :mqtt/unsubscribe
   (fn [_ [_ topic]]
     (send-message! {:message/type :app.cmd/unsubscribe
                     :message/topic topic})))

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
)
