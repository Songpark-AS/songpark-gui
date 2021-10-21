(ns web-app.message
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [songpark.common.protocol.message :as protocol.message]
            [web-app.message.dispatch :as dispatch]
            [songpark.common.protocol.mqtt.manager :as protocol.mqtt.manager]
            [goog.object :as gobj]))

(comment
  (send-message!* @store {:message/type :app.cmd/subscribe
                          :message/topics ["foo" "bar"]
                          })
  (pr-str (gobj/get (:client (:client (:mqtt @store))) "onMessageArrived"))
  (protocol.mqtt.manager/publish (:mqtt @store) "foo" {:message/type :teleporter.msg/info
                                                       :message/body "AUHSFIUAGYWIYF"})
  )

(defonce store (atom nil))

(defn send-message!* [message-service msg]
  (let [injections (-> message-service
                       (select-keys (:injection-ks message-service))
                       (assoc :message-service message-service))]
    (dispatch/handler (merge msg injections))))


(defrecord MessageService [injection-ks started? mqtt]
  component/Lifecycle
  (start [this]
    (if started?
      this
      (do (log/debug "Starting MessageService")
          (let [new-this (assoc this :started? true)]
            (reset! store new-this)
            new-this))))
  (stop [this]
    (if-not started?
      this
      (do (log/info "Stopping MessageService")
          (let [new-this (assoc this
                                :started? false)]
            (reset! store nil)
            new-this))))

  protocol.message/IMessageService
  (send-message! [this msg]
    (send-message!* this msg)))

(defn message-service [settings]
  (map->MessageService settings))
