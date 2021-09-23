(ns web-app.views.session
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [web-app.mqtt :as mqtt]
   ["@material-ui/core/Button$default" :as Button]
   ["@material-ui/core/ButtonGroup$default" :as ButtonGroup]
   ["@material-ui/core/Slider$default" :as Slider]
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


(defn tp-panel [tp-id]
  [:div
   [:h4 "Volume:"]
   [:> Slider {:style (:slider styles)
               :min 0
               :max 1
               :step 0.01
               :default-value 0.8
               :value-label-display "auto"
               :value-label-format (fn [x] (Math/floor (* x 100)))
               :on-change (fn[_ value] (on-volume-value-change tp-id value))}]
   [:h4 "Balance:"]
   [:> Slider {:style (:slider styles)
               :min 0
               :max 1
               :step 0.01
               :default-value 0.5
               :value-label-display "auto"
               :on-change (fn[_ value] (on-balance-value-change tp-id value))}]
   ])

(defn session-controls []
  [:> ButtonGroup
   [:> Button {:color "primary"} "Start session"]
   [:> Button {:color "secondary"} "Stop session"]])

(defn index []
  ;; TODO setup subscription for selected teleporters
  (let [match web-app.views/match]
    (fn []
      [:div
       [:div (str "match: " @match)]
            [:div.session-view
             [:p "teleporters selected, select teleporters "
              [:a {:href (rfe/href :views/teleporter {:id 1})} "here"]]]
            ])
    )
  )
