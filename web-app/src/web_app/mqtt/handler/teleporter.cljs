(ns web-app.mqtt.handler.teleporter
  (:require [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [songpark.mqtt :refer [handle-message]]
            [taoensso.timbre :as log]
            [web-app.utils :as utils]))


(defmethod handle-message :teleporter/heartbeat [{:keys [teleporter/id]}]
  (utils/register-tp-heartbeat id (get-in @config [:heartbeat :timer])))

(defmethod handle-message :teleporter/apt-version [msg]
  (rf/dispatch [:teleporter/apt-version msg]))

(defmethod handle-message :teleporter/net-config-report [msg]
  (rf/dispatch [:teleporter/net-config msg]))

(defmethod handle-message :teleporter/upgrade-status [msg]
  (rf/dispatch [:teleporter/upgrade-status msg]))

(defmethod handle-message :sip/register [msg]
  (rf/dispatch [:teleporter/sip-register msg]))

(defmethod handle-message :teleporter/values [msg]
  (rf/dispatch [:teleporter/values msg]))

(defmethod handle-message :teleporter/hangup-all [msg]
  ;; do nothing at the moment
  )

(defmethod handle-message :teleporter/gain [msg]
  (rf/dispatch [:teleporter/gain msg]))

(defmethod handle-message :teleporter/relay [msg]
  (rf/dispatch [:teleporter/relay msg]))

(defmethod handle-message :teleporter/global-volume [msg]
  (rf/dispatch [:teleporter/global-volume msg]))

(defmethod handle-message :teleporter/local-volume [msg]
  (rf/dispatch [:teleporter/local-volume msg]))

(defmethod handle-message :teleporter/network-volume [msg]
  (rf/dispatch [:teleporter/network-volume msg]))

(defmethod handle-message :teleporter/playout-delay [msg]
  (rf/dispatch [:teleporter/playout-delay msg]))

(defmethod handle-message :teleporter/path-reset [msg]
  ;; do nothing
  )
