(ns web-app.mqtt
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [cognitect.transit :as transit]
            [goog.object :as gobj]
            ["paho-mqtt" :as Paho]
            [songpark.common.protocol.mqtt.client :as protocol.mqtt.client]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [songpark.common.config :as config]
            [songpark.common.communication :refer [writer]]
            [nano-id.core :refer [nano-id]]
            [web-app.mqtt.client :refer [mqtt-client]]))

(def reader (transit/reader :json))

(defn- ->transit [m]
  (transit/write writer m)
  )
(defn- <-transit [s]
  (transit/read reader s))

(comment
  (<-transit (->transit {:foo "bar"}))
  (transit/read reader (transit/write writer {:foo "bar"}))
  (message-handler (transit/write writer #js {:destinationName "World" :payloadString {:message/type :some.cmd/test
                                                                                       :message/body {:this/id 1212}}}))
  )


(defn handle-message [message]
  (log/debug ::handle-message "message: " message))

(defn on-message [message]
  (let [payload (<-transit ^String(.-payloadString message))
        topic ^String(.-destinationName message)]
    (->> (merge payload {:message/topic topic})
         handle-message)))

(defn- subscribe* [{:keys [client] :as mqtt-manager} topics]
    (protocol.mqtt.client/subscribe client topics on-message))

(defn- unsubscribe* [{:keys [client] :as mqtt-manager} topics]
  (doseq [topic topics]
    (protocol.mqtt.client/unsubscribe client topic)))

(defn- publish* [{:keys [client] :as mqtt-manager} topic message]
  (let [transit-formatted-message (->transit message)]
    (protocol.mqtt.client/publish client topic transit-formatted-message)))

(defrecord MQTTManager [started? host port client-id-prefix username password]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (let [client (mqtt-client {:host host :port port :client-id (str client-id-prefix "-" (nano-id 10)) :username username :password password :on-message on-message})
            test-topic-handler (fn [message]
                                 (let [payload ^String (. message -payloadString)]
                                   (prn "Test topic handler")
                                   (prn (str "payload: " payload))))]
        (log/info "Starting MQTTManager")
        (log/info "Connecting to broker")
        (protocol.mqtt.client/connect client)
        #_(protocol.mqtt.client/subscribe client "hello" test-topic-handler)
        (assoc this :started? true :client client))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping MQTTManager")
          (let [client (:client this)]
            (when (protocol.mqtt.client/connected? client)
              (protocol.mqtt.client/disconnect client))))))
  protocol.mqtt.manager/IMqttManager
  (subscribe [this topics]
    (subscribe* this topics))
  (unsubscribe [this topics]
    (unsubscribe* this topics))
  (publish [this topic msg]
    (publish* this topic msg)))


(defn mqtt-manager [settings]
  (map->MQTTManager settings))
