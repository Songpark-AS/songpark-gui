(ns web-app.init
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [songpark.common.taxonomy.teleporter]
            ;; [songpark.common.taxonomy.mqtt]
            ;; [songpark.common.config :as config]
            ;; [app.config :as app.config]
            [web-app.communication :as communication]
            [web-app.event :as event]
            ;; [app.i18n :as i18n]
            [web-app.logging :as logging]
            [web-app.mqtt :as mqtt]
            [web-app.message :as message]
            [web-app.api :as api]
            ["/web_app/config" :as config]
            ))

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
(comment
  (let [webapp-config (js->clj config)]
    (:mqtt config))

  )
(defn- system-map [config-settings]
  (let [#_#_config-manager (component/start (app.config/config-manager config-settings))
        webapp-config (js->clj config :keywordize-keys true)]
    (log/debug ::system-map "webapp-config: " webapp-config)
    (component/system-map
     ;; :config-manager config-manager

     :init          (component/using
                     (init-manager (:init webapp-config))
                     [])
     :logging-manager (component/using
                       (logging/logging-manager (:logging-manager webapp-config))
                       [])
     :event-manager (component/using
                     (event/event-manager (:event-manager webapp-config))
                     [])
     ;; :i18n-manager (component/using
     ;;                (i18n/i18n-manager (:i18n-manager @webapp-config/config))
     ;;                [])
     :communication-manager (component/using
                             (communication/communication-manager (:communication-manager webapp-config))
                             [])
     :api-manager (component/using
                   (api/api-manager {:injection-ks [:message-service]})
                   [:message-service])
     :mqtt-manager (component/using
                    (mqtt/mqtt-manager (:mqtt webapp-config))
                    [])
     :message-service (component/using
                       (message/message-service {:injection-ks [:mqtt-manager]})
                       [:mqtt-manager]))))

(defn init [config-settings]
  (log/info "Initializing system")
  (reset! -system (component/start (system-map config-settings))))
