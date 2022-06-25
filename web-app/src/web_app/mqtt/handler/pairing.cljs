(ns web-app.mqtt.handler.pairing
  (:require [re-frame.core :as rf]
            [songpark.mqtt :refer [handle-message]]))

(defmethod handle-message :pairing/paired [msg]
  (rf/dispatch [:pairing/paired msg]))

(defmethod handle-message :pairing/unpaired [msg]
  (rf/dispatch [:pairing/unpaired msg]))
