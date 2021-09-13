(ns app.i18n
  (:require [com.stuartsierra.component :as component]
            [songpark.common.i18n :as i18n]
            [app.i18n.dicts :refer [dictionary]]
            [tongue.core :as tongue]
            [taoensso.timbre :as log]))

(defn- init-i18n! []
  (i18n/init-i18n dictionary))

(comment
  (init-i18n!)
  )

(defrecord I18nManager [started?]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting I18nManager")
          (init-i18n!)
          (log/info "i18n loaded")
          (assoc this
                 :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping I18nManager")
          (assoc this
                 :started? false)))))

(defn i18n-manager [settings]
  (map->I18nManager settings))
