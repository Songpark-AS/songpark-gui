(ns web-app.views.session
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [web-app.mqtt :as mqtt]
   ["antd" :refer [Button Slider]]
   ))

;; Here be session view
;; Select two teleporters with views.teleporter.list

;; This view will contain
;; View controls of both teleporters
;; Start/stop session buttons


(def styles {:teleporters-container {:display :flex
                                     :flex-direction :row
                                     :align-items :center
                                     :justify-content :space-around}
             })

(def times (atom ()))
(def last-time (atom 0))
(def topic "d7d52eea-c597-4a90-bd4d-abfad567075d")

(defn on-volume-value-change [tp value]
  (log/debug ::on-volume-value-change (str "Change volume of tp: " tp " to: " value))
  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (mqtt/publish! topic (str {:pointer [:tpx-unit :adjust-volume-unit] :arguments [value]}))
    (reset! last-time (system-time)))
  )

(defn on-balance-value-change [tp value]
  (log/debug ::on-balance-value-change (str "Change balance of tp: " tp " to: " value)))


(defn tp-panel [teleporter]
  (let [uuid (:uuid teleporter)
        nickname (:nickname teleporter)]
    [:div.tp-panel {:key (str "tp-panel-" uuid)}
     [:h2 nickname]
     [:h4 "Volume:"]
     [:> Slider {:style (:slider styles)
                 :min 0
                 :max 1
                 :step 0.01
                 :default-value 0.8
                 :tip-formatter (fn [x] (Math/floor (* x 100)))
                 :on-change (fn [value] (on-volume-value-change uuid value))}]
     [:h4 "Balance:"]
     [:> Slider {:style (:slider styles)
                 :min 0
                 :max 1
                 :step 0.01
                 :default-value 0.5
                 :on-change (fn [value] (on-balance-value-change uuid value))}]
     ]))

(defn session-controls []
  [:div.session-controls
   [:> Button {:type "primary"} "Start session"]
   [:> Button {:type "danger"} "Stop session"]])


(defn index []
  (let [selected-teleporters @(rf/subscribe [:selected-teleporters])
        num-selected-teleporters (count selected-teleporters)]
    [:div.session-view
     [:h1 "Session view"]
     (when (>= num-selected-teleporters 2)
       [session-controls])
     (if (> num-selected-teleporters 0)
       (map tp-panel selected-teleporters)
       [:p "No teleporters selected, select teleporters "
        [:a {:href (rfe/href :views/teleporters)} "here"]])
     ])
  )
