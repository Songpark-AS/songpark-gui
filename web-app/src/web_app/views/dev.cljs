(ns web-app.views.dev
  (:require ["antd" :refer [Button Card Radio.Group Radio.Button Divider Switch Slider]]
            [clojure.string :as str]
            [songpark.common.config :refer [config]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web-app.utils :refer [scale-value clamp-value is-touch?]]
            [reitit.frontend.easy :as rfe]
            ["semver" :as semver]
            [taoensso.timbre :as log]
            [web-app.db :as db]
            [web-app.forms.ipv4 :as ipv4-form]))

(defn wrap-semver-lt
  ([^js semver x y]
   (if-not (or (or (nil? x) (nil? y))
               (or (= x "") (= x "Unknown"))
               (or (= y "") (= y "Unknown")))
     (.lt semver x y)
     false)))

(defn scroll-tps-to-pos [pos]
  (let [tps-element (first (js/document.getElementsByClassName "tps"))
        tp-btn-width "10rem"
        tp-btn-gap "0.8rem"]
    (set!
     (.. tps-element -style -left)
     (str "calc(50% - (" tp-btn-width " / 2 + " tp-btn-gap " * " pos " + " tp-btn-width " * " pos "))"))))

(defn slider [props]
  (let [{:keys [label overloading? mode
                playout-delay latency
                min max]} props]
    [:div {:class (if overloading?
                    "sp-slider overload"
                    "sp-slider")}
     [:span.label label]
     [:div.sp-slider-clip
      [:> Slider (assoc props
                        :on-after-change (fn [_]
                                           (js/setTimeout
                                            #(rf/dispatch [:teleporter.setting/clear!])
                                            3000)))]]
     (when (= mode "playoutdelay")
       [:div.pd-display
        [:span.pd-value (str playout-delay " ms")]
        (when latency
          [:span.latency
           [:span.latency-triangle.triangle-up {:style
                                                {:left (str
                                                        (clamp-value
                                                         (scale-value (js/parseFloat latency) [min max] [0 100]) 0 100) "%")}}]
           [:span.latency-text (str "Measured: " latency " ms")]])])]))

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
                              (rf/dispatch [:component/clear!])
                              (swap! state assoc :pos new-pos)))
                          (when (and (= direction "right") (> pos 0))
                            (let [new-pos (dec pos)]
                              (scroll-tps-to-pos new-pos)
                              (select-teleporter @teleporters new-pos)
                              (rf/dispatch [:component/clear!])
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
                                   (rf/dispatch [:component/clear!])
                                   ;; select teleporter
                                   (select-teleporter tp))}
            nickname]))]])))

(def default-scroll-area-position -50)
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

(defn network-details [teleporter]
  (let [tp-id (:teleporter/id @teleporter)
        network-config (rf/subscribe [:teleporter/net-config tp-id])
        network-choice (rf/subscribe [:component/radio-group :ipv4 :dhcp])]
    ;;(log/debug "Hit " tp-id (pr-str @network-config))
    [:div.network
     [:> Radio.Group {:value @network-choice
                      :buttonStyle "solid"}
      [:> Radio.Button {:value :manual
                        :on-click #(rf/dispatch [:component/radio-group :ipv4 :manual])}
       "Manual"]
      [:> Radio.Button {:value :dhcp
                        :on-click #(do
                                     (rf/dispatch [:component/radio-group :ipv4 :dhcp])
                                     (when (false? (:ip/dhcp? @network-config))
                                       (rf/dispatch [:teleporter/save-ipv4
                                                     tp-id
                                                     {:ip/dhcp? true}])))}
       "DHCP"]]
     [:div
      (if (= @network-choice :dhcp)
        (let [{:ip/keys [address gateway subnet]} (db/get-in [:teleporters tp-id :teleporter/net-config])]
          [:div.network-details
           [:div "IP: " address]
           [:div "Net mask: " subnet]
           [:div "Gateway: " gateway]])
        (when (not (nil? @network-config))
          [ipv4-form/ipv4-config tp-id @network-config]))]]))

(defn show-jam-status [jam-status]
  (let [{:jam/keys [status sync sip stream with]} @jam-status]
    [:div.status
     (if (or (= status :idle)
             (= status :jam/waiting))
       "Not in jam"
       (let [out (into ["In jam" with] (->> [sync stream]
                                            (remove nil?)
                                            (map name)))]
         (str/join " • " out)))]))


(defn- handle-upgrade-failed [tp-id]
  (rf/dispatch [:teleporter/upgrade-status {:teleporter/id tp-id :teleporter/upgrade-status "failed"}]))


(defn- on-upgrade-click [tp-id]
  ;; start upgrade timeout
  (rf/dispatch [:teleporter/upgrade-timeout tp-id (js/setTimeout #(handle-upgrade-failed tp-id) (get @config :upgrade_timeout))])

  ;; Send upgrade request to teleporter
  (rf/dispatch [:teleporter/request-upgrade tp-id])

  ;; We are now upgrading the teleporter
  (rf/dispatch [:teleporter/upgrading? tp-id true]))


(defn teleporter-details [teleporter jam-status]
  (r/with-let [swipe-state (r/atom {:start-x 0 :end-x 0 :direction nil :closed? true})
               css-class (r/atom nil)
               tp-scroller-styling (r/atom default-scroll-area-position)
               platform-version (rf/subscribe [:platform/version])
               platform-apt-version (rf/subscribe [:platform/latest-available-apt-version])]
    (let [tp-id (:teleporter/id @teleporter)
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
                                        (+ default-scroll-area-position
                                           (*
                                            -1
                                            (js/Math.min
                                             diff
                                             scroll-area-limit))))
                                (and (neg? diff)
                                     (= direction :down)
                                     (true? closed?))
                                (reset! tp-scroller-styling
                                        (+ default-scroll-area-position
                                           (js/Math.min
                                            (* -1 diff)
                                            scroll-area-limit)))
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
                                                             closed?)})))
           :on-click (fn [e]
                       (let [{:keys [closed?]} @swipe-state]
                         (when-not (is-touch?)
                           (if @css-class
                             (reset! css-class nil)
                             (reset! css-class "show")))
                         (reset! swipe-state {:closed? (not closed?)})))}]
      (when (= @css-class "show")
        (rf/dispatch [:teleporter/request-network-config (:teleporter/id @teleporter)])
        (rf/dispatch [:platform/fetch-latest-available-apt-version]))
      [:div.tp-details
       [:div.board {:class @css-class}
        [:> Card {:bordered false}
         [network-details teleporter]
         [:div.versions
          [:span "VERSIONS"]
          [:div "Teleporter: " (:teleporter/apt-version @teleporter)]
          [:div "Platform: " @platform-version]
          [:div "App: " (:version @config)]]
         [:div.commands
          (let [{:keys [teleporter/apt-version]} @teleporter
                latest-available-apt-version @platform-apt-version]
            (when (wrap-semver-lt semver apt-version latest-available-apt-version)
              [:div {:on-click #(on-upgrade-click tp-id)}
               "Upgrade available. Click to upgrade"]))
          [:> Button {:blocked nil
                      :on-click #(rf/dispatch [:mqtt/send-message-to-teleporter
                                               tp-id
                                               {:message/type :teleporter.cmd/path-reset
                                                :teleporter/id tp-id}])}
           "Reset"]
          ;; temporarily removed because Christian doesn't like swedes :(
          ;; [:> Button {:blocked nil
          ;;             :on-click #(rf/dispatch [:mqtt/send-message-to-teleporter
          ;;                                 tp-id
          ;;                                 {:message/type :teleporter.cmd/hangup-all
          ;;                                  :teleporter/id tp-id}])}
          ;;  "Stop all streams"]
          [:> Button {:blocked nil
                      :on-click #(rf/dispatch [:mqtt/send-message-to-teleporter
                                               tp-id
                                               {:message/type :teleporter.cmd/reboot
                                                :teleporter/id tp-id}])}
           "Reboot"]]]]
       [:div.bottom-border {:style {:margin-top (str @tp-scroller-styling "px")}}
        (let [{:jam/keys [status sip stream sync]} @jam-status
              props (cond (= status :idle)
                          {:on-click #(rf/dispatch [:jam/ask tp-id])
                           :src "/img/logo-idle.png"}
                          (= status :jam/waiting)
                          {:on-click #(rf/dispatch [:jam/obviate tp-id])
                           :src "/img/logo-ask.png"}
                          (= status :jamming)
                          {:on-click #(rf/dispatch [:jam/stop-by-teleporter-id tp-id])
                           :src "/img/logo-jam.png"}
                          (= status :stopping)
                          {:src "/img/logo-stop.png"}
                          :else
                          {:src "/img/logo-idle.png"})]
          [:img.logo props])]
       [:div.scroll-area scroll-area-props]])))

(def max-slider-value 50)

(defn show-playout-delay [tp-id value coredump-data]
  (let [latency (:Latency @coredump-data)]
    [slider {:label "PLAYOUT DELAY"
             :key :playout-delay
             :tooltipVisible false
             :draggableTrack true
             :value @value
             :on-change #(rf/dispatch [:teleporter/setting
                                       tp-id
                                       :jam/playout-delay
                                       %
                                       {:message/type :teleporter.cmd/set-playout-delay
                                        :teleporter/playout-delay %
                                        :teleporter/id tp-id}])
             :min 0
             :max 32
             :playout-delay @value
             :mode "playoutdelay"
             :latency latency}]))

(defn show-volumes [tp-id master-value local-value network-value]
  [:<>
   [slider {:label "MASTER"
            :tooltipVisible false
            :value @master-value
            :max max-slider-value
            :on-change #(rf/dispatch [:teleporter/setting
                                      tp-id
                                      :volume/global-volume
                                      %
                                      {:message/type :teleporter.cmd/global-volume
                                       :teleporter/volume %
                                       :teleporter/id tp-id}])}]
   [slider {:label "LOCAL"
            :tooltipVisible false
            :value @local-value
            :max max-slider-value
            :on-change #(rf/dispatch [:teleporter/setting
                                      tp-id
                                      :volume/local-volume
                                      %
                                      {:message/type :teleporter.cmd/local-volume
                                       :teleporter/volume %
                                       :teleporter/id tp-id}])}]
   [slider {:label "NETWORK"
            :tooltipVisible false
            :value @network-value
            :max max-slider-value
            :on-change #(rf/dispatch [:teleporter/setting
                                      tp-id
                                      :volume/network-volume
                                      %
                                      {:message/type :teleporter.cmd/network-volume
                                       :teleporter/volume %
                                       :teleporter/id tp-id}])}]])


(def ^:private high-z-combo {:line false
                             :instrument true})
(def ^:private high-z-combo-inverted {false "line"
                                      true "instrument"})
(def ^:private phonic-combo {:stereo true
                             :mono false})
(def ^:private phonic-combo-inverted {false "mono"
                                      true "stereo"} )

;; For info on which relay belongs to which control,
;; look into the analog
(defn analog-controls [tp-id {:keys [relay0
                                     relay1
                                     relay2
                                     relay5
                                     left-overloading?
                                     right-overloading?
                                     left-gain
                                     right-gain]}]
  [:> Card {:bordered false}
   [:<>
    [:> Radio.Group {:defaultValue "stereo"
                     :value (phonic-combo-inverted @relay2)
                     :buttonStyle "solid"
                     :on-change #(let [value (-> % .-target .-value keyword phonic-combo)]
                                   (rf/dispatch [:teleporter/setting
                                                 tp-id
                                                 ;; does this exist?
                                                 ;; relay 2 is mute headphones (at the moment)
                                                 :analog/relay2
                                                 value
                                                 {:message/type :teleporter.cmd/relay
                                                  :teleporter/relay :analog/relay2
                                                  :teleporter/value value
                                                  :teleporter/id tp-id}]))}
     [:> Radio.Button {:value "mono"} "Mono"]
     [:> Radio.Button {:value "stereo"} "Stereo"]]
    [:div.v-switch
     [:span.label "48V"]
     [:> Switch {:on-click #(rf/dispatch [:teleporter/setting
                                          tp-id
                                          :analog/relay5
                                          %
                                          {:message/type :teleporter.cmd/relay
                                           :teleporter/relay :analog/relay5
                                           :teleporter/value %
                                           :teleporter/id tp-id}])
                 :checked @relay5
                 :unCheckedChildren "OFF"
                 :checkedChildren "ON"}]]
    [:> Divider {:orientation "left"} "LEFT"]
    [:> Radio.Group {:defaultValue "line"
                     :buttonStyle "solid"
                     :value (high-z-combo-inverted @relay0)
                     :on-change #(let [value (-> % .-target .-value keyword high-z-combo)]
                                   (rf/dispatch [:teleporter/setting
                                                 tp-id
                                                 :analog/relay0
                                                 value
                                                 {:message/type :teleporter.cmd/relay
                                                  :teleporter/relay :analog/relay0
                                                  :teleporter/value value
                                                  :teleporter/id tp-id}]))}
     [:> Radio.Button {:value "instrument"} "Instrument"]
     [:> Radio.Button {:value "line"} "Line"]]
    [slider {:label "GAIN"
             :tooltipVisible false
             :defaultValue 30
             :value @left-gain
             :min 0
             :max 56
             :overloading? @left-overloading?
             :on-change #(rf/dispatch [:teleporter/setting
                                       tp-id
                                       :analog/left-gain
                                       %
                                       {:message/type :teleporter.cmd/gain
                                        :teleporter/gain :analog/left-gain
                                        :teleporter/value %
                                        :teleporter/id tp-id}])}]
    [:> Divider {:orientation "right"} "RIGHT"]
    [:> Radio.Group {:defaultValue "line"
                     :buttonStyle "solid"
                     :value (high-z-combo-inverted @relay1)
                     :on-change #(let [value (-> % .-target .-value keyword high-z-combo)]
                                   (rf/dispatch [:teleporter/setting
                                                 tp-id
                                                 :analog/relay1
                                                 value
                                                 {:message/type :teleporter.cmd/relay
                                                  :teleporter/relay :analog/relay1
                                                  :teleporter/value value
                                                  :teleporter/id tp-id}]))}
     [:> Radio.Button {:value "instrument"} "Instrument"]
     [:> Radio.Button {:value "line"} "Line"]]
    [slider {:label "GAIN"
             :tooltipVisible false
             :defaultValue 30
             :value @right-gain
             :min 0
             :max 56
             :overloading? @right-overloading?
             :on-change #(rf/dispatch [:teleporter/setting
                                       tp-id
                                       :analog/right-gain
                                       %
                                       {:message/type :teleporter.cmd/gain
                                        :teleporter/gain :analog/right-gain
                                        :teleporter/value %
                                        :teleporter/id tp-id}])}]]])

(defn teleporter-controls [teleporter]
  (let [{tp-id :teleporter/id :as tp} @teleporter]
    [:div.cards
     [:> Card {:bordered false}
      [show-volumes
       tp-id
       (rf/subscribe [:teleporter/setting tp-id :volume/global-volume])
       (rf/subscribe [:teleporter/setting tp-id :volume/local-volume])
       (rf/subscribe [:teleporter/setting tp-id :volume/network-volume])]]

     [:> Card {:bordered false}
      [show-playout-delay
       tp-id
       (rf/subscribe [:teleporter/setting tp-id :jam/playout-delay])
       (rf/subscribe [:teleporter/coredump tp-id])]]

     [analog-controls
      tp-id
      {:relay0 (rf/subscribe [:teleporter/setting tp-id :analog/relay0])
       :relay1 (rf/subscribe [:teleporter/setting tp-id :analog/relay1])
       :relay2 (rf/subscribe [:teleporter/setting tp-id :analog/relay2])
       :relay5 (rf/subscribe [:teleporter/setting tp-id :analog/relay5])
       :left-overloading? (rf/subscribe [:teleporter/setting tp-id :analog/left-overloading?])
       :right-overloading? (rf/subscribe [:teleporter/setting tp-id :analog/right-overloading?])
       :left-gain (rf/subscribe [:teleporter/setting tp-id :analog/left-gain])
       :right-gain (rf/subscribe [:teleporter/setting tp-id :analog/right-gain])}]]))

(defn index []
  (r/with-let [teleporters (rf/subscribe [:teleporters])
               teleporter (rf/subscribe [:teleporter.view/selected-teleporter])
               jam-status (rf/subscribe [:teleporter/jam-status])]
    [:div.dev
     [:div.topbar
      [teleporter-switcher teleporters]
      [show-jam-status jam-status]]
     [teleporter-details teleporter jam-status]
     [teleporter-controls teleporter]]))

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
                 (prn tp)) tps)
  (for [[idx tp] (map-indexed identity tps)]
    (log/debug idx)
    (log/debug tp)
    (log/debug tps))
  )
