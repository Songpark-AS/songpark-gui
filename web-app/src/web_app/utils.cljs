(ns web-app.utils
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]))

(defn generate_nickname []
  (let [nouns ["Hope" "Dream" "Party" "Jam"]
        persons ["Mathias" "Achim" "Magnus" "Thor_Atle" "Christian" "Jan_William" "Sindre" "Ronny" "Emil" "Kenneth" "Thanks" "Alf-Gunnar" "Daniel"]
        person (rand-nth persons)
        person-last-letter (clojure.string/join (take-last 1 person))
        noun (rand-nth nouns)]
    (str person (when (not (= person-last-letter "s")) "s") "." noun)
    ))

(defn teleporter-factory []
  {:uuid (cljs.core/random-uuid)
   :nickname (generate_nickname)})

(defn get-random-teleporters [num-teleporters]
  (into [] (repeatedly num-teleporters teleporter-factory)))

(defn is-touch?
  "Returns true if this device supports touch events"
  []
  ;; this is considered bad practice. If we can find
  ;; another way to do it, we should consider it
  (js* "'ontouchstart' in window"))

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
  (log/debug ::register-tp-heartbeat "heartbeat from" tp-id)
  ;; cancel existing offline-timeout if it exists
  (let [offline-timeout (rf/subscribe [:teleporter/offline-timeout tp-id])]
    (when-not (nil? @offline-timeout)
        (js/clearTimeout @offline-timeout)))

  ;; set status to online on this tp
  (rf/dispatch [:teleporter/online? tp-id true])

  ;; register a timeout function to set the status to offline
  (rf/dispatch [:teleporter/offline-timeout tp-id (js/setTimeout #(rf/dispatch [:teleporter/online? tp-id false]) timeout-ms)]))
