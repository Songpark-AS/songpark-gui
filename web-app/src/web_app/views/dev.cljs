(ns web-app.views.dev
  (:require ["antd" :refer [Button Card Radio.Group Radio.Button Divider Switch Slider]]
            [songpark.common.config :refer [config]]
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
  (let [{:keys [label overloading? mode
                playout-delay pd-measured]} props]
    [:div {:class (if overloading? "sp-slider overload" "sp-slider")}
     [:span.label label]
     [:div.sp-slider-clip
      [:> Slider props]]
     (when (= mode "playoutdelay")
       [:div.pd-display
        [:span.pd-value (str playout-delay "ms")]
        (when pd-measured
          [:span.pd-measured
           [:span.pd-measured-triangle.triangle-up]
           [:span.pd-measured-text (str "Measured: " pd-measured "ms")]])])]))

(defn- select-teleporter
  ([teleporter]
   (rf/dispatch [:teleporter.view/select-teleporter
                 (:teleporter/id teleporter)]))
  ([teleporters position]
   (select-teleporter (nth teleporters position))))

(defn teleporter-switcher [teleporters]
  (r/with-let [swipe-state (r/atom {:width 0 :start-x 0 :end-x 0 :direction nil})
               state (r/atom {:pos 0})]
    (let [count-teleporters (count @teleporters)]
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
                         (when (and (= direction "left") (< pos (- count-teleporters 1)))
                           (let [new-pos (inc pos)]
                             (scroll-tps-to-pos new-pos)
                             (select-teleporter @teleporters new-pos)
                             (swap! state assoc :pos new-pos)))
                         (when (and (= direction "right") (> pos 0))
                           (let [new-pos (dec pos)]
                             (scroll-tps-to-pos new-pos)
                             (select-teleporter @teleporters new-pos)
                             (swap! state assoc :pos new-pos)))))}
      [:div.tps
       (doall
        (for [[idx {:teleporter/keys [id nickname] :as tp}] (map-indexed vector @teleporters)]
          [:> Button {:key [:teleporter-switch id]
                      :class (when (= id
                                      (:teleporter/id (nth @teleporters (:pos @state))))
                               "active")
                      :type "primary"
                      :shape "round"
                      :size "large"
                      :on-click (fn [_]
                                  ;; scroll to pos
                                  (swap! state assoc :pos idx)
                                  (scroll-tps-to-pos idx)
                                  ;; select teleporter
                                  (select-teleporter tp))}
           nickname]))]])))

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

(defn teleporter-details []
  (r/with-let [swipe-state (r/atom {:start-x 0 :end-x 0 :direction nil :closed? true})
               css-class (r/atom nil)
               tp-scroller-styling (r/atom default-scroll-area-position)
               teleporter (rf/subscribe [:teleporter.view/selected-teleporter])
               platform-version (rf/subscribe [:platform/version])]
    (let [base-bottom -20
          scroll-area-props
          {:on-touch-start (fn [e]
                             (log/debug :on-touch-start @swipe-state)
                             (let [y (aget e "touches" 0 "pageY")]
                               (swap! swipe-state assoc
                                      :start-y y
                                      :end-y y)))
           :on-touch-move (fn [e]
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
      [:div.tp-details
       [:p.board {:class @css-class}
        [:> Card {:bordered false}
         [:div.versions
          [:span "VERSIONS"]
          [:div "Teleporter: " (:teleporter/apt-version @teleporter)]
          [:div "Platform: " @platform-version]
          [:div "App: " (:version @config)]]
         [:div.commands
          [:div {:on-click #(let[tp-id (:teleporter/id @teleporter)]
                              (rf/dispatch [:mqtt/send-message-to-teleporter
                                            tp-id
                                            {:message/type :teleporter.cmd/path-reset
                                             :teleporter/id tp-id}]))}
           "Reset"]
          [:div {:on-click #(let[tp-id (:teleporter/id @teleporter)]
                              (rf/dispatch [:mqtt/send-message-to-teleporter
                                            tp-id
                                            {:message/type :teleporter.cmd/reboot
                                             :teleporter/id tp-id}]))}
           "Reboot"]]]]
       [:div.bottom-border {:style {:margin-top (str @tp-scroller-styling "px")}}
        [:img.logo {:on-click #(js/alert "I work!")
                    :src "/favicon.ico"}]]
       [:div.scroll-area scroll-area-props]])))

(def max-slider-value 11)

(defn teleporter-controls []
  (r/with-let [teleporter (rf/subscribe [:teleporter.view/selected-teleporter])]
    (let [{tp-id :teleporter/id :as tp} @teleporter]
      [:<>
       [:> Card {:bordered false}
        [:<>
         (for [[label k] [["MASTER" :volume/global-volume]
                          ["LOCAL" :volume/local-volume]
                          ["NETWORK" :volume/network-volume]]]
           ^{:key [:slider k]}
           [slider {:label label
                    :tooltipVisible false
                    :value (get tp k (int (/ max-slider-value 2)))
                    :max max-slider-value
                    :on-change #(rf/dispatch [:teleporter/setting
                                              tp-id
                                              k
                                              %
                                              {:message/type (keyword "teleporter.cmd" (name k))
                                               :teleporter/volume %
                                               :teleporter/id tp-id}])}])]]
       
       [:> Card {:bordered false}
        [:<>
         [slider {:label "PLAYOUT DELAY"
                  :key :playout-delay
                  :tooltipVisible false
                  :draggableTrack true
                  :value (:jam/playout-delay tp)
                  :on-change #(rf/dispatch [:teleporter/setting
                                            tp-id
                                            :jam/playout-delay
                                            %
                                            {:message/type :teleporter.cmd/set-playout-delay
                                             :teleporter/playout-delay %
                                             :teleporter/id tp-id}])
                  :max 32
                  :playout-delay (:jam/playout-delay @teleporter)
                  :mode "playoutdelay"
                  :pd-measured 6}]]]
       
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
         [slider {:label "GAIN" :tooltipVisible false :defaultValue 80 :overloading? true}]]]])))

(defn index []
  (r/with-let [teleporters (rf/subscribe [:teleporters])]
    [:div.dev
     [:div.topbar 
      [teleporter-switcher teleporters]
      [:div.status "In jam - Jimi Hendrix"]]
     ;; [teleporter-switcher (into [] (vals @(rf/subscribe [:teleporters])))]
     [teleporter-details]
     [teleporter-controls]]))

(comment
  (def tps
    [{:teleporter/id "tid1" :teleporter/nickname "Adele"}
     {:teleporter/id "tid2" :teleporter/nickname "Jimi Hendrix"}
     {:teleporter/id "tid3" :teleporter/nickname "Elvis Presley"}
     {:teleporter/id "tid4" :teleporter/nickname "Madonna"}
     {:teleporter/id "tid5" :teleporter/nickname "Beatles"}])

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
