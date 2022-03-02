(ns web-app.mqtt.interceptor
  (:require [re-frame.core :as rf]))

(defonce client (atom nil))

(def mqtt-client
  (rf/->interceptor
   :id :mqtt-client
   :before (fn [context]
             (assoc-in context [:coeffects :mqtt-client] @client))))
