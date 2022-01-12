(ns web-app.init
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.taxonomy.teleporter]
            [songpark.common.config :as songpark.config]
            [web-app.communication :as communication]
            [web-app.config :as config]
            [web-app.event :as event]
            [web-app.api :as api]
            [web-app.logging :as logging]
            [web-app.message :as message]
            [web-app.mqtt :as mqtt]))

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
                             [:config-manager])
     :api-manager (component/using
                   (api/api-manager {:injection-ks [:message-service]})
                   [:message-service])
     :mqtt-manager (component/using
                    (mqtt/mqtt-manager (:mqtt @songpark.config/config))
                    [:config-manager])
     :message-service (component/using
                       (message/message-service {:injection-ks [:mqtt-manager]})
                       [:mqtt-manager]))))

(defn init [config-settings]
  (log/info "Initializing system")
  (reset! -system (component/start (system-map config-settings))))
