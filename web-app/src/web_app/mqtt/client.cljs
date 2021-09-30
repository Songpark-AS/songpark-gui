(ns web-app.mqtt.client
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.protocol.mqtt.client :as protocol.mqtt.client]
            [com.stuartsierra.component :as component]
            [songpark.common.config :as config]
            ["paho-mqtt" :as Paho]))

(defn topic-to-keyword [topic]
  (keyword (clojure.string/replace topic #"/" "_-_")))
(defn keyword-to-topic [keyword]
  (clojure.string/replace (name keyword) #"_-_" "/"))

(defn on-message-receive [message topic-handlers]
  (log/info ::MqttClient.receive "[MQTT rx]: " ^String(. message -payloadString))
  (let [destination-name ^String(. message -destinationName)]
    ;; (map topic-handlers (fn [keyword] (when match destination-name (run thing))))
    (map (keys @topic-handlers))
    ((get @topic-handlers (topic-to-keyword destination-name)) message)))

(defn on-connect [invocation-context mqtt-client]
  (log/debug ::MqttClient.on-connect "Connected to broker, " invocation-context)
  (let [topic-handlers (:topic-handlers mqtt-client)
        client (:client mqtt-client)]
    (when (not (nil? @topic-handlers))
      (log/debug ::MqttClient.on-connect "Resubscribing to topics")
      (doseq [[k f] @topic-handlers]
        (do (protocol.mqtt.client/subscribe client (keyword-to-topic k) f))))))

(defrecord MqttClient [config]
  protocol.mqtt.client/IMqttClient
  (connect [this]
    (let [client ^Paho/Client(:client this)
          topic-handlers (:topic-handlers this)]
      (log/debug ::MqttClient.connect (js->clj client :keywordize-keys true))
      (.connect client #js {:userName "webapp"
                            :password "SecretPass"
                            :reconnect true
                            :onSuccess #(on-connect % this)})
      #_(reset! topic-handlers {})
      #_(-> (.connect client)
          (.then (fn []
                   (reset! connection-lost-event-listener listener-fn)
                   (.on client "connectionLost" listener-fn)
                   )))

      #_(-> (.connect client)
          (.catch  #(log/error ::MqttClient.connect %)))))
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
  (subscribe [this topic on-message]
    (let [client (:client this)
          topic-handlers (:topic-handlers this)]
      (when (protocol.mqtt.client/connected? this)
        (log/debug ::MqttClient.subscribe "Subscribing to topic: " topic)
        (.subscribe client topic)
        (swap! topic-handlers assoc (topic-to-keyword topic) on-message)
        #_(-> (.subscribe client topic)
            (.then #(log/debug ::MqttClient.subscribe "Subscribed to topic: " topic))
            (.then #(swap! topic-handlers assoc (topic-to-keyword topic) on-message))
            (.catch js/Error #(log/error ::MqttClient.subscribe %))))))
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
      :client (let [{:keys [host port client-id]} config
                    client (Paho/Client. host port client-id)]
                (set! (.-onConnectionLost client) (fn [& args] (log/debug ::mqtt-client "Lost connection" args)))
                (set! (.-onMessageArrived client) (fn [message] (on-message-receive message topic-handlers)))
                #_(.on client "messageReceived" (fn [message] (on-message-receive message topic-handlers)))
                client)})))


;; (let [mqtt-settings (:mqtt-settings @config/config)]
;;   (def client (mqtt-client mqtt-settings)))

;; (connect)

