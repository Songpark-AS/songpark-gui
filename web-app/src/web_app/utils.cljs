(ns web-app.utils
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [songpark.common.config :refer [config]]
            [taoensso.timbre :as log]
            [web-app.db :as db]))

(defn kw->str [x]
  (if (keyword? x)
    (subs (str x) 1)
    x))

(defn str->kw [x]
  (if (string? x)
    (keyword x)
    x))

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

(defn get-room
  "Get the room from the query parametersx"
  ([]
   (get-room js/window.location.search))
  ([text]
   (when (and (not (str/blank? text))
              (str/starts-with? text "?"))
     (subs text 1))))

(defn clear-room!
  "Clear room from the query parameters"
  []
  (when-not (str/blank? js/window.location.search)
    (let [new-url (str js/window.location.protocol
                       "//"
                       js/window.location.host
                       js/window.location.pathname
                       js/window.location.hash)]
      (js/window.history.pushState #js {:path new-url} "" new-url))))
