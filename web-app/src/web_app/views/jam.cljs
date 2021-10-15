(ns web-app.views.jam
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [web-app.mqtt :as mqtt]
   [web-app.api :refer [send-message!]]
   [web-app.utils :refer [scale-value]]
   ["antd" :refer [Button Slider]]
   ))

;; Here be jam view
;; Select two teleporters with views.teleporter.list

;; This view will contain
;; View controls of both teleporters
;; Start/stop jam buttons


(def styles {:teleporters-container {:display :flex
                                     :flex-direction :row
                                     :align-items :center
                                     :justify-content :space-around}
             })

(def times (atom ()))
(def last-time (atom 0))
(def topic "d7d52eea-c597-4a90-bd4d-abfad567075d")

(defn on-volume-value-change [topic value]
  (log/debug ::on-volume-value-change (str "Change volume of tp on topic: " topic " to: " value))
  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (send-message! {:message/type :teleporter.cmd.volume/global
                    :message/topic topic
                    :message/body {:message/type :teleporter.cmd.volume/global
                                   :teleporter/volume value}})
    (reset! last-time (system-time)))
  )

(defn on-balance-value-change [topic value]
  (log/debug ::on-balance-value-change (str "Change balance of tp on topic: " topic " to: " value))

  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (send-message! {:message/type :teleporter.cmd/balance
                    :message/topic topic
                    :message/body {:message/type :teleporter.cmd/balance
                                   :teleporter/balance value}})
    (reset! last-time (system-time)))

  )


(defn tp-panel [{:teleporter/keys [uuid nickname]}]
    [:div.tp-panel {:key (str "tp-panel-" uuid)}
     [:h2 nickname]
     [:h4 "Status:"]
     [:div.tp-status
      [:pre "Volume: 80
Balance: 0"]
      ]
     [:h4 "Volume:"]
     [:> Slider {:style (:slider styles)
                 :min 0
                 :max 1
                 :step 0.01
                 :default-value 0.8
                 :tip-formatter (fn [x] (int (* x 100)))
                 :on-change (fn [value] (on-volume-value-change uuid value))}]
     [:h4 "Balance:"]
     [:> Slider {:style (:slider styles)
                 :min 0
                 :max 1
                 :step 0.01
                 :default-value 0.5
                 :tip-formatter (fn [x] (int (scale-value x [0 1] [-50 50])))
                 :on-change (fn [value] (on-balance-value-change uuid value))}]
     ])

(defn start-jam []
  (let [selected-teleporters @(rf/subscribe [:selected-teleporters])]
    (rf/dispatch [:start-jam (mapv #(select-keys % [:teleporter/uuid]) selected-teleporters)])
    )
  )

(defn stop-jam [jam]
  (log/debug ::stop-jam jam)
  (rf/dispatch [:stop-jam (:jam/uuid jam)]))

(defn jam-controls []
  (let [jam (rf/subscribe [:jam])
        started? (rf/subscribe [:jam/started?])]
    [:div.jam-controls
     (if (not (true? @started?))
       [:> Button {:type "primary" :on-click #(start-jam)} "Start jam"]
       [:> Button {:type "danger" :on-click #(stop-jam @jam) } "Stop jam"])]))



(defn index []
  (let [selected-teleporters (rf/subscribe [:selected-teleporters])
        jam (rf/subscribe [:jam])
        started? (rf/subscribe [:jam/started?])
        num-selected-teleporters (count (keys @selected-teleporters))]
    [:div.jam-view
     [:h1 "Jam view"]
     (when (>= num-selected-teleporters 2)
       [jam-controls])
     (if (> num-selected-teleporters 0)
       ;; (map tp-panel @selected-teleporters)
       (for [[_ tp] @selected-teleporters]
         [tp-panel tp]
         )
       [:p "No teleporters selected, select teleporters "
        [:a {:href (rfe/href :views/teleporters)} "here"]])
     ])
  )
