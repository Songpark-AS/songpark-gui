(ns web-app.mqtt.handler.jam
  (:require [re-frame.core :as rf]
            [songpark.mqtt :refer [handle-message]]))


(defmethod handle-message :jam/started [msg]
  (rf/dispatch [:jam/started msg]))

(defmethod handle-message :jam/stopped [msg]
  (rf/dispatch [:jam/stopped msg]))

(defmethod handle-message :jam/waiting [{:keys [teleporter/id]}]
  (rf/dispatch [:teleporter/jam-status id :jam/waiting]))

(defmethod handle-message :jam/obviated [{:keys [teleporter/id]}]
  (rf/dispatch [:teleporter/jam-status id :idle]))

(defmethod handle-message :jam/ask-timed-out [{:keys [teleporter/id]}]
  (rf/dispatch [:teleporter/jam-status id :idle]))

(defmethod handle-message :jam.teleporter/status [msg]
  (rf/dispatch [:jam.teleporter/status msg]))

(defmethod handle-message :jam.teleporter/leaving [msg]
  ;;(rf/dispatch [:jam.teleporter/leaving msg])
  ;; do nothing for now
  )
