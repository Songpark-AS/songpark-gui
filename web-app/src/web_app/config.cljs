(ns web-app.config
  (:require [songpark.common.config :as config]
            ["/web_app/config" :as webapp-config]))

(def debug?
  ^boolean goog.DEBUG)


;; (defrecord ConfigManager [started?]
;;   component/Lifecycle
;;   (start [this]
;;     (if started?
;;       this
;;       (do (log/info "Starting ConfigManager")
;;           (log/info "Resetting config/config")
;;           (reset! config/config (merge config webapp-config))
;;           (assoc this
;;                  :started? true))))
;;   (stop [this]
;;     (if-not started?
;;       this
;;       (do (log/info "Stopping ConfigManager")
;;           (reset! config/config {})
;;           (assoc this
;;                  :started? false)))))

;; (defn config-manager [settings]
;;   (map->ConfigManager {:config-settings settings}))
