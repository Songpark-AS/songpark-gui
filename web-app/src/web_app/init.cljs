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
            [web-app.mqtt.handler.teleporter]
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

(defn init-manager [settings]
  (map->InitManager settings))

(defn- system-map [config-settings]
  (let [config-manager (component/start (config/config-manager config-settings))
        mqtt-client (component/start (mqtt/mqtt-client {:config (:mqtt @songpark.config/config)}))]
    
    (reset! mqtt.int/client mqtt-client)
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
                             [:config-manager])
     :mqtt-client mqtt-client)))

(defn init [config-settings]
  (log/info "Initializing system")
  (reset! -system (component/start (system-map config-settings)))
  ;; subscribe to jam
  (try
    (mqtt/subscribe (:mqtt-client @-system) "jam" 0)
    (catch js/Error e
      (log/error e))))
