(ns web-app.api
  (:require [com.stuartsierra.component :as component]
            [songpark.common.protocol.message :as protocol.message]
            [taoensso.timbre :as log]))

(defonce ^:private store (atom nil))

(defn send-message! [msg]
  (let [api @store
        injections (-> api
                       (select-keys (:injection-ks api))
                       (assoc :api api))]
    #_(protocol.message/send-message! (:message-service injections) msg)
    (log/debug "sending message" msg)))

(defrecord ApiManager [injection-ks started? mqtt]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/info "Starting ApiManager")
          (let [new-this (assoc this
                                :started? true)]
            (reset! store new-this)
            new-this))))

  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping ApiManager")
          (let [new-this (assoc this
                                :started? false)]
            (reset! store nil)
            new-this)))))

(defn api-manager [settings]
  (map->ApiManager settings))
