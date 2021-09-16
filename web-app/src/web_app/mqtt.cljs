(ns web-app.mqtt
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.protocols.mqtt :as protocol.mqtt]
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
        (do (protocol.mqtt/subscribe client (keyword-to-topic k) f))))
    )
  )

#_(defn retry-connection [client]
  (when (not (protocol.mqtt/is-connected? client))
    (log/info ::retry-connection "Trying to reconnect to MQTT broker")
    (-> (retry #(protocol.mqtt/connect client) 10 1000)
        (.then #((log/debug ::retry-connection "Resubscribing to topics")
                 (log/debug ::retry-connection "doseq thing: "
                            (doseq [[k f] @(:topic-handlers client)]
                              (do (protocol.mqtt/subscribe client (keyword-to-topic k) f))))))
        (.catch js/Error #(log/error ::retry-connection %)))))

#_(defn on-connection-lost [response-object client]
  (log/info ::on-connection-lost "Lost connection to MQTT broker" (.-errorCode response-object) (.-errorMessage response-object))
  (let [errorCode (.-errorCode response-object)]
    (when (not (= errorCode 0))
      (log/debug ::on-connection-lost "ErrorCode is not 0, we should try to reconnect")
      (retry-connection client))))

(defn- on-success [response]
  (log/info ::on-success response))

(defn- on-error [reject]
  (log/error reject))


(defrecord MqttClient [config]
  protocol.mqtt/IMqtt
  (connect [this]
    (let [client ^Paho/Client(:client this)
          topic-handlers (:topic-handlers this)]
      (log/debug ::MqttClient.connect (js->clj client :keywordize-keys true))
      (.connect client #js {:reconnect true :onSuccess #(on-connect % this)})
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
        (swap! topic-handlers dissoc (topic-to-keyword topic))
        ))
    ))



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






(comment
  (:mqtt-settings @config/config)
  retry
  config
  protocol.mqtt/connect
  (defn foo-handler [message]
    (let [payload ^String(. message -payloadString)]
      (prn "this is the footopic handler")
      (prn (str "Here is the payload: " payload))
      ))
  (defn world-handler [message]
    (let [payload ^String(. message -payloadString)]
      (prn "this is the world handler")
      (prn (str "Here is the payload: " payload))
      ))
  (defn my-handler [_]
    (prn "hello I am my-handler"))

  (let [mqtt-settings (:mqtt-settings @config/config)]
    (def client (mqtt-client mqtt-settings)))

  (let [mqtt-settings {:host "127.0.0.1"
                       :port 8000
                       :client-id "this-should-be-auto-generated"}]
    (def client (mqtt-client mqtt-settings)))
  client
  (count @(:topic-handlers client))
  on-connection-lost
  @(:connection-lost-event-listener client)
  (.removeListener (:client client) "connectionLost" @(:connection-lost-event-listener client))
  (protocol.mqtt/is-connected? client)
  (:client client)
  (protocol.mqtt/connect client)
  (let [p-client (:client client)]
    (.on p-client "connectionLost" (fn [response-object] (on-connection-lost response-object p-client))))
  (let [p-client (:client client)
        connection-lost-event-listener (:connection-lost-event-listener client)
        listener-fn (fn [response-object] (on-connection-lost response-object p-client))]
    (.on p-client "connectionLost" listener-fn)
    (reset! connection-lost-event-listener listener-fn))
    (-> (protocol.mqtt/connect client)
      (.catch #(log/error "Muh err0r" %)))
  (retry #(protocol.mqtt/connect client) 10 1000)
  (protocol.mqtt/disconnect client)
  (protocol.mqtt/subscribe client "world" world-handler)
  (protocol.mqtt/subscribe client "footopic" foo-handler)
  (protocol.mqtt/subscribe client "multi/level/topic" foo-handler)
  (protocol.mqtt/unsubscribe client "multi/level/topic")
  (protocol.mqtt/unsubscribe client "wild/foobar")
  (protocol.mqtt/unsubscribe client "world")
  (protocol.mqtt/send! client "World" "Hello")

  (let [m (Message. "World")
        _ (set! (.-destinationName m) "World")]
    (pr-str (js->clj m))
    ;;(send! mqc "World" m)
    )

  )
