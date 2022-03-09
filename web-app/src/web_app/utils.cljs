(ns web-app.utils
  (:require [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [taoensso.timbre :as log]
            [web-app.db :as db]))

(defn get-api-url [path]
  (str (:host (:platform @config))
       ":"
       (:port (:platform @config))
       (:api_base (:platform @config))
       path))

(defn get-platform-url [path]
  (str (:host (:platform @config))
       ":"
       (:port (:platform @config))
       path))


(defn is-touch?
  "Returns true if this device supports touch events"
  []
  ;; this is considered bad practice. If we can find
  ;; another way to do it, we should consider it
  (js* "'ontouchstart' in window"))

(defn clamp-value [num min max]
  (js/Math.min (js/Math.max num min) max))

(defn scale-value
  "Linearly transforms x from range input-range to output-range where:

   input-range - a vector like [min max]
   output-range - a vector like [min max]

   "
  [x input-range output-range]
  (let [[a b] input-range
        [c d] output-range]
    (+
     (-> (- x a)
         (/ (- b a))
         - ; negate the result
         inc
         (* c))
     (-> (- x a)
         (/ (- b a))
         (* d)))))

(defn register-tp-heartbeat [tp-id timeout-ms]
  (log/debug ::register-tp-heartbeat "heartbeat from" tp-id "with timeout-ms" timeout-ms)
  ;; cancel existing offline-timeout if it exists
  (let [offline-timeout (db/get-in [:teleporters tp-id :teleporter/offline-timeout])]
    (when-not (nil? offline-timeout)
      (log/debug "Clearing offline timout")
      (js/clearTimeout offline-timeout)))

  ;; set status to online on this tp
  (rf/dispatch [:teleporter/online? tp-id true])

  ;; register a timeout function to set the status to offline
  (let [timeout-obj (js/setTimeout #(rf/dispatch [:teleporter/online? tp-id false])
                                   timeout-ms)]
    (rf/dispatch [:teleporter/offline-timeout tp-id timeout-obj])))
