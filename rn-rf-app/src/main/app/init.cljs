(ns app.init
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.taxonomy.teleporter]
            [songpark.common.taxonomy.mqtt]
            [songpark.common.config :as config]
            [app.config :as app.config]
            [app.communication :as communication]
            [app.event :as event]
            [app.i18n :as i18n]
            [app.logging :as logging]
            [app.mqtt :as mqtt]))

(defonce -system (atom nil))


(defrecord InitManager [started?]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting InitManager")
          (log/debug "config? " (:mqtt-settings @config/config))
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
  (let [config-manager (component/start (app.config/config-manager config-settings))]
    (component/system-map
     :config-manager config-manager

     :init          (component/using
                     (init-manager (:init @config/config))
                     [])
     :logging-manager (component/using
                       (logging/logging-manager (:logging-manager @config/config))
                       [])
     :event-manager (component/using
                     (event/event-manager (:event-manager @config/config))
                     [])
     :i18n-manager (component/using
                    (i18n/i18n-manager (:i18n-manager @config/config))
                    [])
     :communication-manager (component/using
                             (communication/communication-manager (:communication-manager @config/config))
                             [])
     :mqtt-manager (component/using
                    (mqtt/mqtt-manager (:mqtt-settings @config/config))
                    []))))

(defn init [config-settings]
  (log/info "Initializing system")
  (reset! -system (component/start (system-map config-settings))))
