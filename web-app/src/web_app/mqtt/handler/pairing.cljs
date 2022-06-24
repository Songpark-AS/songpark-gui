(ns web-app.mqtt.handler.pairing
  (:require [re-frame.core :as rf]
            [songpark.mqtt :refer [handle-message]]))

(defmethod handle-message :pairing/paired [msg]
  (rf/dispatch [:pairing/paired msg]))
