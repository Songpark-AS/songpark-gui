(ns web-app.init
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [songpark.common.config :as songpark.config]
            [songpark.mqtt :as mqtt]
            [songpark.taxonomy.teleporter]
            [taoensso.timbre :as log]
            [web-app.communication :as communication]
            [web-app.config :as config]
            [web-app.event :as event]
            [web-app.mqtt.interceptor :as mqtt.int]
            [web-app.mqtt.handler.jam]
            [web-app.mqtt.handler.pairing]
            [web-app.mqtt.handler.teleporter]
            [web-app.subs]
            [web-app.logging :as logging]))

(defonce -system (atom nil))


(defrecord InitManager [started?]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting InitManager")
          (assoc this
                 :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping InitManager")
          (assoc this
                 :started? false)))))

(defn start-mqtt [client-id]
  (when (and (some? client-id)
             (not= client-id @-system :mqtt-client :id))
    (let [mqtt-config (:mqtt @songpark.config/config)
          config {:config
                  (merge mqtt-config
                         {:id client-id
                          :connect-options (select-keys
                                            mqtt-config
                                            [:useSSL :reconnect])})}
          mqtt-client (component/start (mqtt/mqtt-client config))]
      (reset! mqtt.int/client mqtt-client)
      (swap! -system assoc :mqtt-client mqtt-client))))

(defn stop-mqtt []
  (when-let [mqtt-client (:mqtt-client @-system)]
    (component/stop mqtt-client)
    (swap! -system dissoc :mqtt-client)))

(defn init-manager [settings]
  (map->InitManager settings))

(defn- system-map [config-settings]
  (let [config-manager (component/start (config/config-manager config-settings))]
    (component/system-map
     :config-manager config-manager

     :init          (component/using
                     (init-manager (:init @songpark.config/config))
                     [])
     :logging-manager (component/using
                       (logging/logging-manager (:logging-manager @songpark.config/config))
                       [])
     :event-manager (component/using
                     (event/event-manager (:event-manager @songpark.config/config))
                     [:config-manager])
     :communication-manager (component/using
                             (communication/communication-manager (:communication-manager @songpark.config/config))
                             [:config-manager]))))

(defn init [config-settings]
  (log/info "Initializing system")
  (reset! -system (component/start (system-map config-settings)))
  ;; subscribe to jam
  (try
    (mqtt/subscribe (:mqtt-client @-system) "jam" 2)
    (catch js/Error e
      (log/error e))))
