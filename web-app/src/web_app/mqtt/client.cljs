(ns web-app.mqtt.client
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.client :as protocol.mqtt.client]
            [com.stuartsierra.component :as component]
            [songpark.common.config :as config]
            ["paho-mqtt" :as Paho]
            ["antd" :refer [message]]))

(defn topic-to-keyword [topic]
  (keyword (clojure.string/replace topic #"/" "_-_")))
(defn keyword-to-topic [keyword]
  (clojure.string/replace (name keyword) #"_-_" "/"))


(defn on-connect [invocation-context mqtt-client]
  (log/debug ::MqttClient.on-connect "Connected to broker, " invocation-context)
  (.success message "Connected to MQTT")
  (let [topic-handlers (:topic-handlers mqtt-client)]
    (when (not (empty? @topic-handlers))
      (log/debug ::MqttClient.on-connect "Resubscribing to topics")

      ;; hack
      (let [f (val (first @topic-handlers))
            topics (map keyword-to-topic (keys @topic-handlers))]
        (protocol.mqtt.client/subscribe mqtt-client topics f))

      #_(doseq [[k f] @topic-handlers]
          (do (protocol.mqtt.client/subscribe client (keyword-to-topic k) f))))))

(defn on-connection-lost [& args]
  (log/debug ::on-connection-lost "Lost connection to mqtt: " args)
  (.error message "Lost connection to MQTT"))

(defrecord MqttClient [config]
  protocol.mqtt.client/IMqttClient
  (connect [this]
    (let [client ^Paho/Client(:client this)]
      (log/debug ::MqttClient.connect (js->clj client :keywordize-keys true))
      (.connect client #js {:userName (:username config)
                            :password (:password config)
                            :reconnect true
                            :onSuccess #(on-connect % this)})
      ))
  (connected? [this]
    (.isConnected (:client this)))
  (publish [this topic message]
    (let [client (:client this)
          _message (Paho/Message. message)]
      (set! (.-destinationName  _message) topic)
      (try
        (.send client _message)
        (catch js/Error e (log/error ::MqttClient.publish e)))))
  (disconnect [this]
    (.disconnect (:client this)))
  (subscribe [this topics on-message]
    (let [client (:client this)
          topic-handlers (:topic-handlers this)]
      (when (protocol.mqtt.client/connected? this)
        (doseq [topic topics]
          (log/debug ::MqttClient.subscribe "Subscribing to topic: " topic)
          (.subscribe client topic)
          (swap! topic-handlers assoc (topic-to-keyword topic) on-message))
        )))
  (unsubscribe [this topic]
    (let [client (:client this)
          topic-handlers (:topic-handlers this)]
      (when (protocol.mqtt.client/connected? this)
        (.unsubscribe client topic)
        (log/debug ::MqttClient.unsubscribe "Unsubscribed from topic: " topic)
        (swap! topic-handlers dissoc (topic-to-keyword topic))))))

(defn mqtt-client [config]
  (map->MqttClient
   (let [topic-handlers (atom {})]
     {:config config
      :topic-handlers topic-handlers
      :client (let [{:keys [host port client-id on-message username password]} config
                    client (Paho/Client. host port client-id)]
                (set! (.-onConnectionLost client) (fn [& args] (on-connection-lost args)))
                (set! (.-onMessageArrived client) (fn [message] (on-message message)))
                client)})))

