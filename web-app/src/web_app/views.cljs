(ns web-app.views
  (:require
   [re-frame.core :as rf]
   [web-app.subs :as subs]
   [taoensso.timbre :as log]
   ["@material-ui/core/Slider$default" :as Slider]))

(def styles {:slider-container {:display :flex}
             :slider {:margin "0 1rem"}})

(defn tp-status []
  (let [teleporter (rf/subscribe [:teleporter/data])
        mqtt (rf/subscribe [:mqtt/data])]
    [:div
     [:h3 "Teleporter data:"]
     [:p (str "Nickname: " (:teleporter/nickname @teleporter))]
     [:p (str "Bits: " (:teleporter/bits @teleporter))]
     [:p (str "UUID: " (:teleporter/uuid @teleporter))]
     [:h3 "MQTT data:"]
     [:p (str "Username: " (:mqtt/username @mqtt))]
     [:p (str "Password: " (:mqtt/password @mqtt))]]))

(defn on-slider-value-change [tp value]
  (log/debug ::on-slider-value-change (str "Change volume of tp: " tp " to: " value)))

(defn main-panel []
  [:div
   [:p [:button {:on-click #(rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])} "Get TP status"]]
   [:p [:button {:on-click #(rf/dispatch [:mqtt/connect])} "Connect MQTT"]]
   [:p [:button {:on-click #(rf/dispatch [:mqtt/publish! "World" "Hello World!"])} "Hello World!"]]
   [tp-status]
   [:div {:style (:slider-container styles)}
    [:> Slider {:style (:slider styles)
                :min 0
                :max 1
                :step 0.01
                :on-change (fn[_ value] (on-slider-value-change "tp1" value))}]
    [:> Slider {:style (:slider styles)
                :min 0
                :max 1
                :step 0.01
                :on-change (fn[_ value] (on-slider-value-change "tp2" value))}]]])
