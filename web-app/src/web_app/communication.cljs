(ns web-app.communication
  (:require [com.stuartsierra.component :as component]
            [songpark.common.communication :as communication]
            [taoensso.timbre :as log]))

(defrecord CommunicationManager [started? url]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting CommunicationManager")
          (log/info "Setting Communication base-url to " url)
          (reset! communication/base-url url)
          (assoc this
                 :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping CommunicationManager")
          (reset! communication/base-url nil)
          (assoc this
                 :started? false)))))

(defn communication-manager [settings]
  (map->CommunicationManager settings))
