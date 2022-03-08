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
                     :class (when (= (:teleporter/uuid tp)
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

(def default-scroll-area-position 0)
(def scroll-area-bump 0)
(def scroll-area-limit 20)

(defn- snap? [direction diff]
  (cond
    (and (= :up direction)
         (pos? diff)
         (>= (js/Math.abs diff) scroll-area-limit))
    :up
    (and (= :down direction)
         (neg? diff)
         (>= (js/Math.abs diff) scroll-area-limit))
    :down

    :else
    nil))

(defn tp-scroller []
  (r/with-let [swipe-state (r/atom {:start-x 0 :end-x 0 :direction nil :closed? true})
               css-class (r/atom nil)
               tp-scroller-styling (r/atom default-scroll-area-position)]
    (let [base-bottom -20
          scroll-area-props
          {:on-touch-start (fn [e]
                             (.stopPropagation e)
                             (log/debug :on-touch-start @swipe-state)
                             (let [y (aget e "touches" 0 "pageY")]
                               (swap! swipe-state assoc
                                      :start-y y
                                      :end-y y)))
           :on-touch-move (fn [e]
                            (.stopPropagation e)
                            (swap! swipe-state assoc :end-y (aget e "touches" 0 "pageY"))
                            (let [{:keys [start-y end-y closed?]} @swipe-state
                                  direction (if (> end-y start-y) :down :up)
                                  diff (- start-y end-y)
                                  snap-direction (snap? direction diff)]
                              (log/debug {:diff diff
                                          :snap-direction snap-direction
                                          :direction direction
                                          :closed? closed?})
                              (cond
                                (and (pos? diff)
                                     (= direction :up)
                                     (false? closed?))
                                (reset! tp-scroller-styling
                                        (*
                                         -1
                                         (js/Math.min
                                          diff
                                          scroll-area-limit)))
                                (and (neg? diff)
                                     (= direction :down)
                                     (true? closed?))
                                (reset! tp-scroller-styling
                                        (js/Math.min
                                         (* -1 diff)
                                         scroll-area-limit))
                                :else
                                (reset! tp-scroller-styling default-scroll-area-position))
                              (swap! swipe-state assoc
                                     :snap-direction snap-direction
                                     :direction direction)))
           :on-touch-end (fn [e]
                           (.preventDefault e)
                           (.stopPropagation e)
                           (let [{:keys [direction
                                         snap-direction
                                         closed?]} @swipe-state]
                             (log/debug :direction direction :css-class @css-class)
                             (log/debug :snap-direction snap-direction)
                             (when (= snap-direction :up)
                               (reset! css-class nil))
                             (when (= snap-direction :down)
                               (reset! css-class "show"))
                             (reset! tp-scroller-styling default-scroll-area-position)
                             (reset! swipe-state {:closed? (condp = snap-direction
                                                             :up true
                                                             :down false
                                                             closed?)})))}]
      [:div.tp-scroller
       [:p.board {:class @css-class}
        
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas ultrices nibh maximus maximus consequat. Donec non ultricies justo, quis viverra dolor. Nullam non dui dapibus, congue lorem in, semper dolor. Sed et consectetur erat. Pellentesque malesuada pellentesque semper. Vestibulum elit massa, finibus sit amet scelerisque nec, eleifend sed lorem. Aenean justo lacus, lacinia dapibus nisi ut, pulvinar faucibus diam. Aliquam volutpat ex sed enim vehicula, at feugiat dui mollis. Praesent iaculis bibendum diam pellentesque vestibulum. In magna odio, scelerisque sit amet ex sit amet, imperdiet elementum mi. Vestibulum id libero eu erat ultricies dignissim id eget dui."

        "Maecenas efficitur sed nisi efficitur suscipit. Ut pellentesque dapibus sapien nec aliquam. Phasellus dapibus, neque vel fringilla facilisis, velit purus semper neque, nec ornare dolor enim vitae lectus. Sed et sem ultrices leo congue cursus eu eget velit. Sed posuere placerat nisi, in luctus libero viverra in. Quisque eget elit facilisis, lobortis mauris sit amet, interdum magna. Proin tempor augue in pellentesque elementum. Pellentesque ut iaculis velit, scelerisque bibendum justo. Mauris non velit varius, condimentum nisl nec, bibendum massa. Proin elementum efficitur nibh in facilisis. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Praesent maximus nunc accumsan turpis suscipit aliquam. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus vehicula dolor nec justo pellentesque suscipit."

        "Donec mattis massa elit. Nullam egestas turpis diam, at lobortis neque volutpat vitae. Donec vel mollis nisl, tempor tempus diam. Nulla tristique, ante id blandit consequat, turpis erat interdum lorem, at laoreet urna lacus vel massa. Aliquam ultrices interdum eros id dignissim. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Duis ac erat in enim cursus tincidunt ac eget justo. Nulla ut urna in lectus facilisis dictum vitae ut diam. Duis at augue sapien. Sed et auctor justo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus."

        "Morbi ut dolor at lorem varius commodo sed at magna. Suspendisse dolor elit, semper eget est et, mollis interdum enim. Vestibulum aliquet quam quam, pharetra euismod lectus bibendum at. Maecenas dictum efficitur nibh, ut ornare elit pharetra vel. Maecenas sodales arcu non hendrerit auctor. Aenean eu varius massa, vitae suscipit est. Phasellus dictum sagittis efficitur. Duis magna nibh, suscipit at orci id, rutrum ultricies ante. Donec posuere quam posuere nisi fermentum, nec condimentum ipsum elementum. Donec euismod fringilla dolor, in lobortis nulla venenatis vitae. Donec at enim sit amet tortor congue aliquet sit amet et urna. Proin malesuada ac nisl sit amet finibus. Aliquam maximus magna ut elit iaculis cursus. Quisque volutpat neque elit, a molestie metus mollis ut."

        "Donec at erat non lectus rutrum tincidunt in a risus. Donec bibendum commodo diam, eu imperdiet diam placerat in. Nullam imperdiet non erat in faucibus. Cras in tristique metus, sed volutpat tellus. Maecenas semper dictum erat, et pretium diam hendrerit eget. Aenean id turpis auctor libero sodales vulputate. Donec sit amet sem augue. Etiam consequat vestibulum euismod. Pellentesque vulputate, diam id cursus convallis, nulla lorem tincidunt ex, vel ullamcorper leo eros quis tortor. Nullam dapibus lectus in justo tincidunt rutrum. Curabitur ac fringilla diam, eget lobortis urna."]
       [:div.bottom-border {:style {:margin-top (str @tp-scroller-styling "px")}}
        [:img.logo {:on-click #(js/alert "I work!")
                    :src "/favicon.ico"}]]
       [:div.scroll-area scroll-area-props]])))

(defn index []
  [:div.dev
   [:div.topbar 
    [teleporter-switcher [{:teleporter/uuid "tid1" :teleporter/nickname "Adele"}
                          {:teleporter/uuid "tid2" :teleporter/nickname "Jimi Hendrix"}
                          {:teleporter/uuid "tid3" :teleporter/nickname "Elvis Presley"}
                          {:teleporter/uuid "tid4" :teleporter/nickname "Madonna"}
                          {:teleporter/uuid "tid5" :teleporter/nickname "Beatles"}]]
    [:div.status "In jam • Jimi Hendrix"]]
   ;; [teleporter-switcher (into [] (vals @(rf/subscribe [:teleporters])))]
   [tp-scroller]
   [:> Card {:bordered false}
    [:<>
     [slider {:label "MASTER" :tooltipVisible false :defaultValue 50}]
     [slider {:label "LOCAL" :tooltipVisible false :defaultValue 50}]
     [slider {:label "NETWORK" :tooltipVisible false :defaultValue 50}]]]
   [:> Card {:bordered false}
    [:<>
     [slider {:label "PLAYOUT DELAY" :tooltipVisible false :defaultValue 65 :mode "playoutdelay" :pd-measured 6}]]]
   [:> Card {:bordered false}
    [:<>
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
     [slider {:label "GAIN" :tooltipVisible false :defaultValue 80 :overloading? true}]]]])

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
