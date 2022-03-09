(ns web-app.event.teleporter
  (:require ["antd" :refer [message notification]]
            [re-frame.core :as rf]
            [songpark.mqtt :as mqtt]
            [songpark.mqtt.util :refer [broadcast-topic
                                        heartbeat-topic
                                        teleporter-topic]]
            [web-app.mqtt.interceptor :refer [mqtt-client]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :init-app
 (fn [_ _]
   {:dispatch [:http/get (get-api-url "/app") nil :handle-init]}))

(rf/reg-event-fx
 :handle-init
 [mqtt-client]
 (fn [{:keys [db mqtt-client] :as cofx} [_ data]]
   (let [{:keys [teleporters jams]} data]
     (doseq [{:keys [teleporter/id]} teleporters]
       (let [bt (broadcast-topic id)
             ht (heartbeat-topic id)]
         (mqtt/subscribe mqtt-client {bt 0
                                      ht 0})))
     {:db (assoc db
                 :teleporters (->> teleporters
                                   (map (juxt :teleporter/id identity))
                                   (into {}))
                 :jams (->> jams
                            (map (juxt :jam/id #(select-keys % [:jam/id :jam/members])))
                            (into {})))})))

(rf/reg-event-fx
 :teleporter/upgrade-status
 (fn [cofx [_ {:keys [teleporter/id teleporter/upgrade-status]}]]
   (let [upgrade-timeout (rf/subscribe [:teleporter/upgrade-timeout id])]
     (when (= upgrade-status "complete")
       (do
         (js/clearTimeout @upgrade-timeout)
         (.success message "Teleporter upgraded successfully!")))
     (when (= upgrade-status "failed")
       (.error notification #js {:message "Error upgrading firmware"
                                 :description "Oops, something went wrong upgrading the firmware of the teleporter!"
                                 :duration 0})))
   {:db (assoc-in (:db cofx) [:teleporters id :teleporter/upgrade-status] upgrade-status)
    :fx [[:dispatch [:teleporter/upgrade-timeout id nil]]
         [:dispatch [:teleporter/upgrading? id false]]]}))

(rf/reg-event-fx
 :teleporter/save-ipv4
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ tp-id values]]
   (mqtt/publish mqtt-client (teleporter-topic tp-id)
                 {:message/type :teleporter.cmd/set-ipv4
                  :teleporter/network-values values
                  :teleporter/id tp-id})
   nil))

(rf/reg-event-db
 :teleporter/sip-register
 (fn [db [_ {:keys [teleporter/id]}]]
   (assoc-in db [:teleporters id :sip/register] true)))

(rf/reg-event-db
 :teleporter/net-config
 (fn [db [_ {:keys [teleporter/id teleporter/network-config]}]]
   (assoc-in db [:teleporters id :teleporter/net-config] network-config)))

(rf/reg-event-db
 :teleporter/coredump
 (fn [db [_ {:keys [teleporter/id teleporter/coredump-data]}]]
   (assoc-in db [:teleporters id :teleporter/coredump] coredump-data)))

(rf/reg-event-db
 :teleporter/apt-version
 (fn [db [_ {:keys [teleporter/id teleporter/apt-version]}]]
   (assoc-in db [:teleporters id :teleporter/apt-version] apt-version)))

(rf/reg-event-db
 :teleporter/offline-timeout
 (fn [db [_ tp-id timeout-obj]]
   (assoc-in db [:teleporters tp-id :teleporter/offline-timeout] timeout-obj)))

(rf/reg-event-db
 :teleporter/upgrade-timeout
 (fn [db [_ tp-id timeout-obj]]
   (assoc-in db [:teleporters tp-id :teleporter/upgrade-timeout] timeout-obj)))

(rf/reg-event-db
 :teleporter/online?
 (fn [db [_ tp-id online?]]
   (assoc-in db [:teleporters tp-id :teleporter/online?] online?)))

(rf/reg-event-db
 :teleporter/upgrading?
 (fn [db [_ tp-id upgrading?]]
   (assoc-in db [:teleporters tp-id :teleporter/upgrading?] upgrading?)))

(rf/reg-event-db
 :teleporter/jam-status
 (fn [db [_ tp-id jam-status]]
   (assoc-in db [:teleporters tp-id :jam/status] jam-status)))


(rf/reg-event-fx
 :teleporter/request-network-config
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ tp-id]]
   (mqtt/publish mqtt-client
                 (teleporter-topic tp-id)
                 {:message/type :teleporter.cmd/report-network-config
                  :teleporter/id tp-id})
   nil))

(rf/reg-event-fx
 :teleporter/request-upgrade
 [mqtt-client]
 (fn [{:keys [mqtt-client]} [_ tp-id]]
   (mqtt/publish mqtt-client
                 (teleporter-topic tp-id)
                 {:message/type :teleporter.cmd/upgrade
                  :teleporter/id tp-id})))

(rf/reg-event-db
 :teleporter/values
 (fn [db [_ {:teleporter/keys [id values]}]]
   (update-in db [:teleporters id] merge values)))


(rf/reg-event-db
 :teleporter/gain
 (fn [db [_ {:teleporter/keys [id gain value]}]]
   (assoc-in db [:teleporters id gain] value)))

(rf/reg-event-db
 :teleporter/relay
 (fn [db [_ {:teleporter/keys [id relay value]}]]
   (assoc-in db [:teleporters id relay] value)))


(rf/reg-event-db
 :teleporter/global-volume
 (fn [db [_ {:teleporter/keys [id global-volume]}]]
   (assoc-in db [:teleporters id :volume/global-volume] global-volume)))

(rf/reg-event-db
 :teleporter/local-volume
 (fn [db [_ {:teleporter/keys [id local-volume]}]]
   (assoc-in db [:teleporters id :volume/local-volume] local-volume)))

(rf/reg-event-db
 :teleporter/network-volume
 (fn [db [_ {:teleporter/keys [id network-volume]}]]
   (assoc-in db [:teleporters id :volume/network-volume] network-volume)))


(rf/reg-event-db
 :teleporter/playout-delay
 (fn [db [_ {:teleporter/keys [id playout-delay]}]]
   (assoc-in db [:teleporters id :jam/playout-delay] playout-delay)))

(rf/reg-event-fx
 :teleporter/setting
 (fn [{:keys [db]} [_ tp-id tp-setting-k tp-setting-v mqtt-msg]]
   {:db (-> db
            (assoc-in [:teleporters tp-id tp-setting-k] tp-setting-v)
            (assoc-in [:teleporters tp-id :teleporter/setting tp-setting-k] tp-setting-v))
    :dispatch [:mqtt/send-message-to-teleporter tp-id mqtt-msg]}))

(rf/reg-event-db
 :teleporter.setting/clear!
 (fn [db _]
   (reduce (fn [db tp-id]
             (assoc-in db [:teleporters tp-id :teleporter/setting] {}))
           db (keys (:teleporters db)))))
