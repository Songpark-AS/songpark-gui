(ns example.mqtt
  (:require
   ["react-native-paho-mqtt" :refer [Client Message]]
   ))
(def mqtt-store (atom {}))

(def mqtt-store-thing (clj->js {
                                :setItem (fn [key, item] (swap! mqtt-store assoc key item))
                                :getItem (fn [key] (get @mqtt-store key))
                                :removeItem (fn [key] (swap! mqtt-store dissoc key))
                                }))

(defn get-client-id []
  ;; TODO
  (str "MyClientId"))

(def mqtt-config {
                  :host "127.0.0.1"
                  :port 8000
                  :topic "World"
                  :storage mqtt-store-thing
})

(let [{host :host
       port :port
       client-id :client-id
       topic :topic
       storage :storage
       } mqtt-config]

  (defonce mqtt-client (Client. #js {:uri (str "ws://" host ":" port "/") :clientId (get-client-id) :storage storage}))
)

(defn on-message-receive [message] (.log js/console (str "[MQTT rx]: " ^String(. message -payloadString))))

(.on mqtt-client "connectionLost" #(prn (str "connection lost" %)))
(.on mqtt-client "messageReceived" on-message-receive)
(defn connect []
  (.then (.connect mqtt-client) #(.subscribe mqtt-client "World"))
)
(defn send! [topic value]
  (def message (Message. value))
  (set! (.. message -destinationName) topic)
  (.send mqtt-client message)
  )
