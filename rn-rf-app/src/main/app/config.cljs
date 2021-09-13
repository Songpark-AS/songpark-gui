(ns app.config
  (:require [com.stuartsierra.component :as component]
            [songpark.common.config :as config]
            [taoensso.timbre :as log]))

(defrecord ConfigManager [started? config-settings]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting ConfigManager")
          (log/info "Resetting config/config")
          (reset! config/config config-settings)
          (assoc this
                 :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping ConfigManager")
          (reset! config/config {})
          (assoc this
                 :started? false)))))

(defn config-manager [settings]
  (map->ConfigManager {:config-settings settings}))
