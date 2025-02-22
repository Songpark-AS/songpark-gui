(ns app.event
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [app.db :as db]
            #_[app.event.auth]
            #_[app.event.data]
            [app.event.ui]))

(rf/reg-event-db ::initialize-db (fn [_ _]
                                   db/default-db))

;; db: we shouldn't get a nil value as dispatch. log it as an error and fix
(rf/reg-event-fx nil (fn [_ & args]
                       (log/error ::nil-event args)
                       nil))

(defrecord EventManager [started?]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting EventManager")
          (rf/dispatch-sync [::initialize-db])
          ;; (rf/dispatch [:auth/whoami]) ;; check if we are logged in
          (assoc this
                 :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping EventManager")
          (assoc this
                 :started? false)))))

(defn event-manager [settings]
  (map->EventManager settings))
