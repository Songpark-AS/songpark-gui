(ns web-app.event.ui
  (:require [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [taoensso.timbre :as log]
            [web-app.api :refer [send-message!]]
            [web-app.data :as data]
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


(defn rotate-log [log log-msg]
  (let [n (max 999 (dec (count log)))]
    (conj (take n log) log-msg)))

(defn- get-api-url [path]
  (str (:host (:platform @config))
       ":"
       (:port (:platform @config))
       (:api_base (:platform @config))
       path))

(defn- get-platform-url [path]
  (str (:host (:platform @config))
       ":"
       (:port (:platform @config))
       path))

(rf/reg-event-db
 :set-tp-list-selection-mode
 (fn [db [_ value]]
   (assoc db :tp-list-selection-mode value)))

(rf/reg-event-fx
 :set-teleporters
 (fn [{:keys [db] :as cofx} [_ teleporters]]
   (send-message! {:message/type :teleporters/listen
                   :message/body teleporters})
   {:db (assoc db :teleporters teleporters)}))

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


(rf/reg-event-fx
 :fetch-platform-version
 (fn [_ _]
   {:dispatch [:http/get (get-platform-url "/version") nil :set-platform-version]}))

(rf/reg-event-db
 :set-platform-version
 (fn [db [_ {:keys [version]}]]
   (assoc db :platform/version version)))

(rf/reg-event-fx
 :subscribe-jam
 (fn [_ [_ jam]]
   (let [uuid (:jam/uuid jam)]
     {:dispatch-n [[:mqtt/subscribe (str uuid)] [:jam/started? true]]})))

(rf/reg-event-fx
 :set-jam
 (fn [cofx [_ jam]]
   (data/set-jam-id! (:jam/id jam))
   {:db (assoc (:db cofx) :jam jam)
    :fx [[:dispatch [:subscribe-jam jam]]]}))

(rf/reg-event-fx
 :on-jam-deleted
 (fn [cofx [_ response]]
   (log/debug ::on-jam-deleted "response" response)
   (let [jam (rf/subscribe [:jam])]
     {:fx [
           [:dispatch [:mqtt/unsubscribe [(str (:jam/uuid @jam))]]]
           [:dispatch [:jam/started? false]]]})))

(rf/reg-event-fx
 :fetch-teleporters
 (fn [_ _]
   {:dispatch [:http/get (get-api-url "/app") nil :set-teleporters]}))

(rf/reg-event-fx
 :start-jam
 (fn [_ [_ uuids]]
   {:dispatch [:http/put (get-api-url "/jam") uuids :set-jam]}))

(rf/reg-event-fx
 :stop-jam
 (fn [_ [_ jam-uuid]]
   {:dispatch [:http/delete (get-api-url "/jam") {:jam/uuid jam-uuid} :on-jam-deleted]}))


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

(rf/reg-event-db
 :teleporter/log
 (fn [db [_ {:keys [log/level teleporter/id] :as log-msg}]]
   (let [log (get-in db [:teleporter/log id level] [])
         n (dec (count log))]
     (assoc-in db [:teleporter/log id level] (rotate-log log log-msg)))))


(rf/reg-event-db
 :view.telemetry.log/teleporter
 (fn [db [_ teleporter-id]]
   (assoc db :view.telemetry.log/teleporter teleporter-id)))

(rf/reg-event-db
 :view.telemetry.log/level
 (fn [db [_ level]]
   (assoc db :view.telemetry.log/level level)))

(rf/reg-event-db
 :view.telemetry/tab
 (fn [db [_ active-tab]]
   (assoc db :view.telemetry/tab active-tab)))

(rf/reg-event-db
 :teleporter.log/clear!
 (fn [db [_ ?teleporter-id ?level]]
   (cond
     (and ?teleporter-id ?level)
     (assoc-in db [:teleporter/log (str ?teleporter-id) ?level] nil)

     ?teleporter-id
     (assoc-in db [:teleporter/log (str ?teleporter-id)] nil)

     :else
     (dissoc db :teleporter/log))))

(rf/reg-event-db
 :teleporter/offline-timeout
 (fn [db [_ tp-id timeout-obj]]
   (assoc-in db [:teleporter/offline-timeout tp-id] timeout-obj)))

(rf/reg-event-db
 :teleporter/online?
 (fn [db [_ tp-id online?]]
   (assoc-in db [:teleporter/online? tp-id] online?)))

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

