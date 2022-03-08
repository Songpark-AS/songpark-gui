(ns web-app.event
  (:require [com.stuartsierra.component :as component]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [web-app.db :as db]
            [web-app.utils :refer [get-random-teleporters]]
            #_[app.event.auth]
            #_[app.event.data]
            [web-app.event.ui]))

(rf/reg-event-db ::initialize-db (fn [_ _]
                                   db/default-db))

;; db: we shouldn't get a nil value as dispatch. log it as an error and fix
(rf/reg-event-fx nil (fn [_ & args]
                       (log/error ::nil-event args)
                       nil))

(defn set-no-contextmenu! []
  (js/document.addEventListener "contextmenu"
                                (fn [e]
                                  (.preventDefault e)
                                  (.stopPropagation e)
                                  false)))


(defrecord EventManager [started?]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting EventManager")
          (rf/dispatch-sync [::initialize-db])
          (rf/dispatch [:fetch-teleporters])
          (rf/dispatch [:fetch-platform-version])
          (log/info "Setting no contextmenu")
          (set-no-contextmenu!)
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
