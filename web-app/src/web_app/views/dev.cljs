(ns web-app.views.dev
  (:require ["antd" :refer [Button Card Radio.Group Radio.Button Divider Switch Slider]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]))

(defn scroll-tps-to-pos [pos]
  (let [tps-element (first (js/document.getElementsByClassName "tps"))]
    (set!
     (.. tps-element -style -left)
     (str "calc(50% - (10rem / 2 + 0.8rem * " pos " + 10rem * " pos "))"))))

(defn slider [props]
  (let [{:keys [label overloading? mode pd-measured]} props]
    (fn [props]
      [:div {:class (if overloading? "sp-slider overload" "sp-slider")}
       [:span.label label]
       [:div.sp-slider-clip
        [:> Slider props]]
       (when (= mode "playoutdelay")
         [:div.pd-display
          [:span.pd-value "12ms"]
          (when pd-measured
            [:span.pd-measured
             [:span.pd-measured-triangle.triangle-up]
             [:span.pd-measured-text (str "Measured: " pd-measured "ms")]])])])))

(defn teleporter-switcher [tps]
  (let [swipe-state (r/atom {:width 0 :start-x 0 :end-x 0 :direction nil})
        state (r/atom {:pos 0})]
    [:div.tps-container
     {:on-touch-start (fn [e]
                        (log/debug @swipe-state)
                        (reset! swipe-state {:width (.. e -target -offsetWidth)
                                                  :start-x (aget e "touches" 0 "pageX")
                                                  :end-x (aget e "touches" 0 "pageX")}))
      :on-touch-move (fn [e] (swap! swipe-state assoc :end-x (aget e "touches" 0 "pageX"))
                      (let [{:keys [width start-x end-x]} @swipe-state
                            direction (if (> end-x start-x) "right" "left")]
                        (swap! swipe-state assoc :direction direction)
                        (log/debug :on-touch-end "start" start-x "end" end-x)
                        ))
      :on-touch-end (fn [_]
                      (let [direction (:direction @swipe-state)
                            pos (:pos @state)]
                        (when (and (= direction "left") (< pos (- (count tps) 1)))
                          (do
                            (scroll-tps-to-pos (inc pos))
                            (swap! state assoc :pos (inc pos))))
                        (when (and (= direction "right") (> pos 0))
                          (do
                            (scroll-tps-to-pos (dec pos))
                            (swap! state assoc :pos (dec pos))))
                        (log/debug :on-touch-end direction)))}
     [:div.tps
      (for [tp tps]
        [:> Button {:key (:teleporter/uuid tp) :type "primary" :shape "round" :size "large"} (:teleporter/nickname tp)])]
     ])
  )

(defn index []
  [:div.dev
   [teleporter-switcher [{:teleporter/uuid "tid1" :teleporter/nickname "Adele"}
                         {:teleporter/uuid "tid2" :teleporter/nickname "Jimi Hendrix"}
                         {:teleporter/uuid "tid3" :teleporter/nickname "Elvis Presley"}
                         {:teleporter/uuid "tid4" :teleporter/nickname "Madonna"}
                         {:teleporter/uuid "tid5" :teleporter/nickname "Beatles"}]]
   ;; [teleporter-switcher (into [] (vals @(rf/subscribe [:teleporters])))]
   ;; [:div.tp-scroller
   ;;  [:p.status "Not in jam"]]
   [:> Card {:bordered false} [:<>
             [slider {:label "MASTER" :tooltipVisible false :defaultValue 50}]
             [slider {:label "LOCAL" :tooltipVisible false :defaultValue 50}]
             [slider {:label "NETWORK" :tooltipVisible false :defaultValue 50}]
             ]]
   [:> Card {:bordered false} [:<>
             [slider {:label "PLAYOUT DELAY" :tooltipVisible false :defaultValue 65 :mode "playoutdelay" :pd-measured 6}]
             ]]
   [:> Card {:bordered false} [:<>
             [:> Radio.Group {:defaultValue "stereo" :buttonStyle "solid"}
              [:> Radio.Button {:value "mono"} "Mono"]
              [:> Radio.Button {:value "stereo"} "Stereo"]]
             [:div.v-switch [:span.label "48V"] [:> Switch {:unCheckedChildren "OFF" :checkedChildren "ON"}]]
             [:> Divider {:orientation "left"} "LEFT"]
             [:> Radio.Group {:defaultValue "line" :buttonStyle "solid"}
              [:> Radio.Button {:value "instrument"} "Instrument"]
              [:> Radio.Button {:value "line"} "Line"]]
             [slider {:label "GAIN" :tooltipVisible false :defaultValue 80}]
             [:> Divider {:orientation "right"} "RIGHT"]
             [:> Radio.Group {:defaultValue "line" :buttonStyle "solid"}
              [:> Radio.Button {:value "instrument"} "Instrument"]
              [:> Radio.Button {:value "line"} "Line"]]
             [slider {:label "GAIN" :tooltipVisible false :defaultValue 80 :overloading? true}]
             ]]
   ])
