(ns web-app.mqtt
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.protocols.mqtt :as protocol.mqtt]
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
        (do (protocol.mqtt/subscribe client (keyword-to-topic k) f))))))

(defrecord MqttClient [config]
  protocol.mqtt/IMqtt
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
  (is-connected? [this]
    (.isConnected (:client this)))
  (publish! [this topic message]
    (let [client (:client this)
          _message (Paho/Message. message)]
      (set! (.-destinationName  _message) topic)
      (try
        (.send client _message)
        (catch js/Error e (log/error ::MqttClient.publish! e)))))
  (disconnect [this]
    (.disconnect (:client this)))
  (subscribe [this topic on-message]
    (let [client (:client this)
          topic-handlers (:topic-handlers this)]
      (when (protocol.mqtt/is-connected? this)
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
      (when (protocol.mqtt/is-connected? this)
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

(defrecord MQTTManager [started? host port client-id]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (let [client (mqtt-client {:host host :port port :client-id client-id})
            test-topic-handler (fn [message]
                                 (let [payload ^String (. message -payloadString)]
                                   (prn "Test topic handler")
                                   (prn (str "payload: " payload))))]
        (log/info "Starting MQTTManager")
        (log/info "Connecting to broker")
        (protocol.mqtt/connect client)
        #_(protocol.mqtt/subscribe client "hello" test-topic-handler)
        (assoc this :started? true :client client))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping MQTTManager")
          (let [client (:client this)]
            (when (protocol.mqtt/is-connected? client)
              (protocol.mqtt/disconnect client)))))))

(defn mqtt-manager [settings]
  (map->MQTTManager settings))

(let [mqtt-settings (:mqtt-settings @config/config)]
  (def client (mqtt-client mqtt-settings)))

(defn connect []
  (when (not (protocol.mqtt/is-connected? client))
    (protocol.mqtt/connect client)))

(connect)

(defn publish! [topic message]
  (protocol.mqtt/publish! client topic message))


(comment

  (defn foo-handler [message]
    (let [payload ^String(. message -payloadString)]
      (prn "this is the footopic handler")
      (prn (str "Here is the payload: " payload))))
  (defn world-handler [message]
    (let [payload ^String(. message -payloadString)]
      (prn "this is the world handler")
      (prn (str "Here is the payload: " payload))))
  (defn my-handler [_]
    (prn "hello I am my-handler"))

  (let [mqtt-settings (:mqtt-settings @config/config)]
    (def client (mqtt-client mqtt-settings)))

  (protocol.mqtt/is-connected? client)
  (protocol.mqtt/connect client)
  (protocol.mqtt/disconnect client)

  (protocol.mqtt/subscribe client "world" world-handler)
  (protocol.mqtt/subscribe client "footopic" foo-handler)
  (protocol.mqtt/subscribe client "multi/level/topic" foo-handler)

  (protocol.mqtt/unsubscribe client "multi/level/topic")
  (protocol.mqtt/unsubscribe client "footopic")
  (protocol.mqtt/unsubscribe client "world")

  (protocol.mqtt/publish! client "World" "Hello")

  )
