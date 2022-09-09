(ns web-app.mqtt.handler.room
  (:require [re-frame.core :as rf]
            [songpark.mqtt :refer [handle-message]]))

(defmethod handle-message :room.jam/knocked [msg]
  (rf/dispatch [:room.jam.mqtt/knocked msg]))

(defmethod handle-message :room.jam/accepted [msg]
  (rf/dispatch [:room.jam.mqtt/accepted msg]))

(defmethod handle-message :room.jam/left [msg]
  (rf/dispatch [:room.jam.mqtt/left msg]))

(defmethod handle-message :room.jam/declined [msg]
  (rf/dispatch [:room.jam.mqtt/declined msg]))

(defmethod handle-message :room.jam/removed [msg]
  (rf/dispatch [:room.jam.mqtt/removed msg]))

(defmethod handle-message :room.jam/closed [msg]
  (rf/dispatch [:room.jam.mqtt/closed msg]))
