(ns web-app.views.input
  (:require ["antd" :refer [Slider
                            Switch
                            Tabs
                            Tabs.TabPane]]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.knob :refer [knob]]))

(defn switch [switch-sub switch-dispatch]
  [:div.switch
   [:> Switch
    {:checkedChildren "On"
     :unCheckedChildren "Off"
     :checked @switch-sub
     :on-change switch-dispatch}]])

(defmulti content-pane :tab)

(defmethod content-pane "Amplify" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [drive (rf/subscribe [:teleporter/fx nil input :amplify/drive])
               tone (rf/subscribe [:teleporter/fx nil input :amplify/tone])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.knobs
      [knob {:skin "light"
             :model drive
             :on-change #(rf/dispatch [:teleporter/fx nil input :amplify/drive %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :amplify/drive
                                        :teleporter/value %}])
             :title "DRIVE"}]
      [knob {:skin "light"
             :model tone
             :value-fn #(str % "%")
             :on-change #(rf/dispatch [:teleporter/fx nil input :amplify/tone %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :amplify/tone
                                        :teleporter/value %}])
             :title "TONE"}]]]))

(defmethod content-pane "Echo" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [delay-time (rf/subscribe [:teleporter/fx nil input :echo/delay-time])
               level (rf/subscribe [:teleporter/fx nil input :echo/level])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.knobs
      [knob {:skin "light"
             :model delay-time
             :value-fn #(str % "ms")
             :value/max 2000
             :on-change #(rf/dispatch [:teleporter/fx nil input :echo/delay-time %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :echo/delay-time
                                        :teleporter/value %}])
             :title "DELAY TIME"}]
      [knob {:skin "light"
             :model level
             :value-fn #(str % "%")
             :on-change #(rf/dispatch [:teleporter/fx nil input :echo/level %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :echo/level
                                        :teleporter/value %}])
             :title "MIX"}]]]))


(defmethod content-pane "Reverb" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [mix (rf/subscribe [:teleporter/fx nil input :reverb/mix])
               damp (rf/subscribe [:teleporter/fx nil input :reverb/damp])
               room-size (rf/subscribe [:teleporter/fx nil input :reverb/room-size])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.knobs
      [knob {:skin "light"
             :model mix
             :on-change #(rf/dispatch [:teleporter/fx nil input :reverb/mix %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :reverb/mix
                                        :teleporter/value %}])
             :title "MIX"}]
      [knob {:skin "light"
             :model damp
             :on-change #(rf/dispatch [:teleporter/fx nil input :reverb/damp %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :reverb/damp
                                        :teleporter/value %}])
             :title "DAMP"}]
      [knob {:skin "light"
             :model room-size
             :value-fn #(str % "%")
             :on-change #(rf/dispatch [:teleporter/fx nil input :reverb/room-size %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :reverb/room-size
                                        :teleporter/value %}])
             :title "ROOM SIZE"}]]]))



(defmethod content-pane "Gate" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [threshold (rf/subscribe [:teleporter/fx nil input :gate/threshold])
               attack (rf/subscribe [:teleporter/fx nil input :gate/attack])
               release (rf/subscribe [:teleporter/fx nil input :gate/release])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.knobs
      [knob {:skin "light"
             :model threshold
             :arc/start 315
             :rotate/end 180
             :value-fn #(str % "dB")
             :value/min 0
             :value/max -30
             :on-change #(rf/dispatch [:teleporter/fx nil input :gate/threshold %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :gate/threshold
                                        :teleporter/value %}])
             :title "THRESHOLD"}]
      [knob {:skin "light"
             :model attack
             :value/max 100
             :value-fn #(str (/ % 10.0) "ms")
             :on-change #(rf/dispatch [:teleporter/fx nil input :gate/attack %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :gate/attack
                                        :teleporter/value %}])
             :title "ATTACK"}]
      [knob {:skin "light"
             :model release
             :value/max 5000
             :value-fn #(str (/ % 10.0) "ms")
             :on-change #(rf/dispatch [:teleporter/fx nil input :gate/release %
                                       {:message/type :teleporter.cmd/fx
                                        :teleporter/input input
                                        :teleporter/fx :gate/release
                                        :teleporter/value %}])
             :title "RELEASE"}]]]))

(defmethod content-pane "Compressor" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [threshold (rf/subscribe [:teleporter/fx nil input :compressor/threshold])
               ratio (rf/subscribe [:teleporter/fx nil input :compressor/ratio])
               attack (rf/subscribe [:teleporter/fx nil input :compressor/attack])
               release (rf/subscribe [:teleporter/fx nil input :compressor/release])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.sliders
      [:div.slider
       ^{:key [input :compressor/threshold]}
       [:> Slider
        {:vertical true
         :min -30
         :max 0
         :tipFormatter #(r/as-element [:div (str % "dB")])
         :default-value @threshold
         :on-change #(rf/dispatch [:teleporter/fx nil input :compressor/threshold %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :compressor/threshold
                                    :teleporter/value %}])}]
       [:h2 "THRESHOLD"]]
      [:div.slider
       ^{:key [input :compressor/ratio]}
       [:> Slider
        {:vertical true
         :min 0
         :max 20
         :tipFormatter #(r/as-element [:div (str % ":1")])
         :default-value @ratio
         :on-change #(rf/dispatch [:teleporter/fx nil input :compressor/ratio %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :compressor/ratio
                                    :teleporter/value %}])}]
       [:h2 "RATIO"]]
      [:div.slider
       ^{:key [input :compressor/attack]}
       [:> Slider
        {:vertical true
         :min 0
         :max 100
         :tipFormatter #(r/as-element [:div (gstring/format "%.1fms" (/ % 10.0))])
         :default-value @attack
         :on-change #(rf/dispatch [:teleporter/fx nil input :compressor/attack %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :compressor/attack
                                    :teleporter/value %}])}]
       [:h2 "ATTACK"]]
      [:div.slider
       ^{:key [input :compressor/release]}
       [:> Slider
        {:vertical true
         :min 0
         :max 5000
         :tipFormatter #(r/as-element [:div (gstring/format "%dms" (/ % 10))])
         :default-value @release
         :on-change #(rf/dispatch [:teleporter/fx nil input :compressor/release %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :compressor/release
                                    :teleporter/value %}])}]
       [:h2 "RELEASE"]]]]))

(defmethod content-pane "Equalizer" [{:keys [switch-sub switch-dispatch input]}]
  (r/with-let [low (rf/subscribe [:teleporter/fx nil input :equalizer/low])
               medium-low (rf/subscribe [:teleporter/fx nil input :equalizer/medium-low])
               medium-high (rf/subscribe [:teleporter/fx nil input :equalizer/medium-high])
               high (rf/subscribe [:teleporter/fx nil input :equalizer/high])
               tip-formatter #(r/as-element [:div (str % "dB")])]
    [:div.fx-content
     [switch switch-sub switch-dispatch]
     [:div.sliders
      [:div.slider
       ^{:key [input :equalizer/low]}
       [:> Slider
        {:vertical true
         :min -10
         :max 10
         :tipFormatter tip-formatter
         :default-value @low
         :on-change #(rf/dispatch [:teleporter/fx nil input :equalizer/low %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :equalizer/low
                                    :teleporter/value %}])}]
       [:h2 "LOW"]]
      [:div.slider
       ^{:key [input :equalizer/medium-low]}
       [:> Slider
        {:vertical true
         :min -10
         :max 10
         :tipFormatter tip-formatter
         :default-value @medium-low
         :on-change #(rf/dispatch [:teleporter/fx nil input :equalizer/medium-low %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :equalizer/medium-low
                                    :teleporter/value %}])}]
       [:h2 "MEDIUM LOW"]]
      [:div.slider
       ^{:key [input :equalizer/medium-high]}
       [:> Slider
        {:vertical true
         :min -10
         :max 10
         :tipFormatter tip-formatter
         :default-value @medium-high
         :on-change #(rf/dispatch [:teleporter/fx nil input :equalizer/medium-high %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :equalizer/medium-high
                                    :teleporter/value %}])}]
       [:h2 "MEDIUM HIGH"]]
      [:div.slider
       ^{:key [input :equalizer/high]}
       [:> Slider
        {:vertical true
         :min -10
         :max 10
         :tipFormatter tip-formatter
         :default-value @high
         :on-change #(rf/dispatch [:teleporter/fx nil input :equalizer/high %
                                   {:message/type :teleporter.cmd/fx
                                    :teleporter/input input
                                    :teleporter/fx :equalizer/high
                                    :teleporter/value %}])}]
       [:h2 "HIGH"]]]]))



(defmethod content-pane :default [_]
  [:div "Default pane. This should not show up"])

(defn- create-switch-dispatch [input fx-k]
  #(rf/dispatch [:teleporter/fx nil input fx-k %
                 {:message/type :teleporter.cmd/fx
                  :teleporter/fx fx-k
                  :teleporter/value %}]))

(defn- create-switch-sub [input fx-k]
  (rf/subscribe [:teleporter/fx
                 nil
                 input fx-k]))

(defn index [matched]
  (let [input (-> matched :data :name name)
        fxs (->> ["Gate"
                  "Reverb"
                  "Amplify"
                  "Equalizer"
                  "Echo"
                  "Compressor"]
                 (map (fn [fx]
                        (let [fx-k (keyword (-> fx str/lower-case) "switch")]
                          {:tab fx
                           :input input
                           :switch-sub (create-switch-sub input fx-k)
                           :switch-dispatch (create-switch-dispatch input fx-k)}))))
        pan-sub (rf/subscribe [:teleporter/fx nil input :pan])
        pan-dispatch #(rf/dispatch [:teleporter/fx nil input :pan %
                                    {:message/type :teleporter.cmd/fx
                                     :teleporter/input input
                                     :teleporter/fx :pan
                                     :teleporter/value %}])
        gain-sub (rf/subscribe [:teleporter/fx nil input :gain])
        gain-dispatch #(rf/dispatch [:teleporter/fx nil input :gain %
                                     {:message/type :teleporter.cmd/fx
                                      :teleporter/input input
                                      :teleporter/fx :gain
                                      :teleporter/value %}])
        overload? (rf/subscribe [:teleporter/fx nil input :overload?])]
    [:div.input
     [:div.input-fx
      ^{:key [input :pan]}
      [knob {:title "PAN"
             :model pan-sub
             :on-change pan-dispatch
             :rotate/start 0
             :rotate/end 180
             :arc/start 270
             :value/min -50
             :value/max 50}]
      ^{:key [input :gain]}
      [knob {:title "GAIN"
             :overload? overload?
             :model gain-sub
             :on-change gain-dispatch
             :value-fn (fn [v] (str v " dB"))}]]
     [:div.output-fx
      [:> Tabs
       {:moreIcon nil}
       [:<>
        (for [fx fxs]
          ^{:key (str input (:tab fx))}
          [:> Tabs.TabPane
           {:tab (:tab fx)
            :key (str input (:tab fx))}
           [:div.inner
            [:h2 (:tab fx)]
            [content-pane fx]]])]]]]))
