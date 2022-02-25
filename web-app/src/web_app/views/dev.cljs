(ns web-app.views.dev
  (:require ["antd" :refer [Button Card Radio.Group Radio.Button Divider Switch Slider Space]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]))


(defn slider [label sliderprops]
  [:div.sp-slider
   [:span.label label]
   [:> Slider sliderprops]])

(defn index []
  [:div.dev
   [:div.tp-scroller
    [:div.tps
     [:> Button {:type "primary" :shape "round" :size "large"} "MADONNA"]
     [:> Button {:type "primary" :shape "round" :size "large"} "JIMI HENDRIX"]
     [:> Button {:type "primary" :shape "round" :size "large"} "ELVIS PRESLEY"]
     [:> Button {:type "primary" :shape "round" :size "large"} "ADELE"]]
    [:p.status "Not in jam"]]
   [:> Card [:<>
             [slider "MASTER" {:tooltipVisible false :defaultValue 50}]
             [slider "LOCAL" {:tooltipVisible false :defaultValue 50}]
             [slider "NETWORK" {:tooltipVisible false :defaultValue 50}]
             ]]
   [:> Card [:<>
             [slider "PLAYOUT DELAY" {:tooltipVisible false :defaultValue 65}]
             ]]
   [:> Card [:<>
             [:> Radio.Group {:defaultValue "stereo" :buttonStyle "solid"}
              [:> Radio.Button {:value "mono"} "Mono"]
              [:> Radio.Button {:value "stereo"} "Stereo"]]
             [:div.v-switch [:span.label "48V"] [:> Switch {:unCheckedChildren "OFF" :checkedChildren "ON"}]]
             [:> Divider {:orientation "left"} "LEFT"]
             [:> Radio.Group {:defaultValue "line" :buttonStyle "solid"}
              [:> Radio.Button {:value "instrument"} "Instrument"]
              [:> Radio.Button {:value "line"} "Line"]]
             [slider "GAIN" {:tooltipVisible false :defaultValue 50}]
             [:> Divider {:orientation "right"} "RIGHT"]
             [:> Radio.Group {:defaultValue "line" :buttonStyle "solid"}
              [:> Radio.Button {:value "instrument"} "Instrument"]
              [:> Radio.Button {:value "line"} "Line"]]
             [slider "GAIN" {:tooltipVisible false :defaultValue 80}]
             ]]
   ])
