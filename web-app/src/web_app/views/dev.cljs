(ns web-app.views.dev
  (:require ["antd" :refer [Button Card Radio.Group Radio.Button Divider Switch Slider]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]))

(defn scroll-tps-to-pos [pos]
  (let [tps-element (first (js/document.getElementsByClassName "tps"))
        tp-btn-width "10rem"
        tp-btn-gap "0.8rem"]
    (set!
     (.. tps-element -style -left)
     (str "calc(50% - (" tp-btn-width " / 2 + " tp-btn-gap " * " pos " + " tp-btn-width " * " pos "))"))))

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
  (r/with-let [swipe-state (r/atom {:width 0 :start-x 0 :end-x 0 :direction nil})
        state (r/atom {:pos 0})]
    [:div.tps-container
     {:on-touch-start (fn [e]
                        (reset! swipe-state {:width (.. e -target -offsetWidth)
                                                  :start-x (aget e "touches" 0 "pageX")
                                                  :end-x (aget e "touches" 0 "pageX")}))
      :on-touch-move (fn [e] (swap! swipe-state assoc :end-x (aget e "touches" 0 "pageX"))
                      (let [{:keys [width start-x end-x]} @swipe-state
                            direction (if (> end-x start-x) "right" "left")]
                        (swap! swipe-state assoc :direction direction)))
      :on-touch-end (fn [_]
                      (let [direction (:direction @swipe-state)
                            pos (:pos @state)]
                        (when (and (= direction "left") (< pos (- (count tps) 1)))
                          (do
                            (scroll-tps-to-pos (inc pos))
                            (rf/dispatch [:selected-teleporter (nth tps (inc pos))])
                            (swap! state assoc :pos (inc pos))))
                        (when (and (= direction "right") (> pos 0))
                          (do
                            (scroll-tps-to-pos (dec pos))
                            (rf/dispatch [:selected-teleporter (nth tps (dec pos))])
                            (swap! state assoc :pos (dec pos))))))}
     [:div.tps
      (doall
       (for [[idx tp] (map-indexed vector tps)]
         [:> Button {:key (:teleporter/uuid tp)
                     :class (when (=
                                   (:teleporter/uuid tp)
                                   (:teleporter/uuid (nth tps (:pos @state))))
                              "active")
                     :type "primary"
                     :shape "round"
                     :size "large"
                     :on-click (fn [_]
                                 ;; scroll to pos
                                 (swap! state assoc :pos idx)
                                 (scroll-tps-to-pos idx)

                                 ;; select teleporter
                                 (rf/dispatch [:selected-teleporter tp]))}
          (:teleporter/nickname tp)]))]]))

(defn index []
  [:div.dev
   [:div.topbar 
    [teleporter-switcher [{:teleporter/uuid "tid1" :teleporter/nickname "Adele"}
                          {:teleporter/uuid "tid2" :teleporter/nickname "Jimi Hendrix"}
                          {:teleporter/uuid "tid3" :teleporter/nickname "Elvis Presley"}
                          {:teleporter/uuid "tid4" :teleporter/nickname "Madonna"}
                          {:teleporter/uuid "tid5" :teleporter/nickname "Beatles"}]]
    [:div.status "In jam - Jimi Hendrix"]]
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

(comment
  (def tps
    [{:teleporter/uuid "tid1" :teleporter/nickname "Adele"}
     {:teleporter/uuid "tid2" :teleporter/nickname "Jimi Hendrix"}
     {:teleporter/uuid "tid3" :teleporter/nickname "Elvis Presley"}
     {:teleporter/uuid "tid4" :teleporter/nickname "Madonna"}
     {:teleporter/uuid "tid5" :teleporter/nickname "Beatles"}])

  (log/debug tps)
  (map-indexed (fn [idx tp]
                 (prn idx)
                 (prn tp)
                 ) tps)
  (for [[idx tp] (map-indexed identity tps)]
    (log/debug idx)
    (log/debug tp)
    (log/debug tps)
    )

  )
