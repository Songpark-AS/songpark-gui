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

(defmethod handle-message :teleporter/coredump [msg]
  (rf/dispatch [:teleporter/coredump msg]))

(defmethod handle-message :teleporter/upgrade-status [msg]
  (rf/dispatch [:teleporter/upgrade-status msg]))

(defmethod handle-message :sip/register [msg]
  (rf/dispatch [:teleporter/sip-register msg]))
