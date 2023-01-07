(ns web-app.mqtt.handler.jam
  (:require [re-frame.core :as rf]
            [songpark.mqtt :refer [handle-message]]))

(defmethod handle-message :jam/started [msg]
  (rf/dispatch [:jam/started msg]))

(defmethod handle-message :jam/stopped [msg]
  (rf/dispatch [:jam/stopped msg]))

(defmethod handle-message :jam/event [msg]
  (rf/dispatch [:jam.teleporter/status msg]))

(defmethod handle-message :jam.teleporter/leaving [msg]
  ;;(rf/dispatch [:jam.teleporter/leaving msg])
  ;; do nothing for now
  )

(defmethod handle-message :jam.teleporter/coredump [msg]
  (rf/dispatch [:jam.teleporter/coredump msg]))
