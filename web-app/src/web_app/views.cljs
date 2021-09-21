(ns web-app.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [web-app.subs :as subscriptions]
   [web-app.mqtt :as mqtt]
   [taoensso.timbre :as log]
   [ez-wire.form :as form]
   ["@material-ui/core/Button$default" :as Button]
   ["@material-ui/core/ButtonGroup$default" :as ButtonGroup]
   ["@material-ui/core/Slider$default" :as Slider])
  (:require-macros [ez-wire.form.macros :refer [defform]]
                   [ez-wire.form.validation :refer [defvalidation]]))

(def times (atom ()))
(def last-time (atom 0))

(defvalidation :teleporter/ip "Invalid IP-address")

(def styles {:teleporters-container {:display :flex
                         :flex-direction :row
                         :align-items :center
                         :justify-content :space-around}
             })


(defn input-text [{:keys [model placeholder disabled]}]
  [:input.input {:type :text
                 :placeholder placeholder
                 :value @model
                 :disabled disabled
                 :on-change #(reset! model (-> % .-target .-value))}])

(defn input-checkbox [{:keys [model]}]
  [:input.input {:type :checkbox
                 :value @model
                 :on-change #(reset! model (-> % .-target .-checked))}])

(defform form-tp-nickname
  {}
  [{:element input-text
    :placeholder "christians.dream"
    :label "Nickname:"
    :name :tp/nickname}])

(defform form-ipv4
  {:branch/branching? true
   :branch/branches {:ip/dhcp? (fn [{:keys [value]}]
                                 (if (not (nil? value))
                                   (if (true? value)
                                     {:fields {:ip/address {:disabled true}
                                               :ip/subnet {:disabled true}
                                               :ip/gateway {:disabled true}}}
                                     {:fields {:ip/address {:disabled false}
                                               :ip/subnet {:disabled false}
                                               :ip/gateway {:disabled false}}})
                                   {:fields {:ip/address {:disabled false}
                                             :ip/subnet {:disabled false}
                                             :ip/gateway {:disabled false}}}))}}
  [{:element input-text
    :placeholder "192.168.0.123"
    :label "IP address:"
    :name :ip/address
    :validation :teleporter/ip}
   {:element input-text
    :placeholder "255.255.255.0"
    :label "Subnet mask:"
    :name :ip/subnet
    :validation :teleporter/ip}
   {:element input-text
    :placeholder "192.168.0.1"
    :label "Gateway address:"
    :name :ip/gateway
    :validation :teleporter/ip}
   {:element input-checkbox
    :label "Use DHCP?:"
    :name :ip/dhcp?}])

(defn ipv4-config [tp-id]
  (let [form (form-ipv4 {} nil)
        data-form (rf/subscribe [:ez-wire.form/on-valid (:id form)])]
    (fn []
      [:div
       [:h4 "Network settings"]
       [form/as-table {} form]
       ])))

(defn tp-nickname [tp-id]
  (let [id (str tp-id "-nickname")
        form (form-tp-nickname {:id id} nil)
        data-form (rf/subscribe [:ez-wire.form/on-valid (:id form)])]
    (fn []
      [:div
       [form/as-table {} form]
       ])))

(defn tp-status [tp-id]
  (let [teleporter (rf/subscribe [:teleporter/data])
        mqtt (rf/subscribe [:mqtt/data])]
    #_[:div
     [:h3 "Teleporter data:"]
     [:p (str "Nickname: " (:teleporter/nickname @teleporter))]
     [:p (str "Bits: " (:teleporter/bits @teleporter))]
     [:p (str "UUID: " (:teleporter/uuid @teleporter))]
     [:h3 "MQTT data:"]
     [:p (str "Username: " (:mqtt/username @mqtt))]
     [:p (str "Password: " (:mqtt/password @mqtt))]]))


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


(defn handle-tp-status-btn-click [tp-id]
  (let [id (str tp-id "-nickname")
        nickname (:tp/nickname @(rf/subscribe [:ez-wire.form/on-valid id]))]
    (rf/dispatch [:teleporter/status {:teleporter/nickname nickname}])
    )
  )

(defn tp-panel [tp-id]
  [:div
   ;; [:p [:> Button {:color "primary" :variant "contained" :on-click #(handle-tp-status-btn-click tp-id)} "Get TP status"]]
   ;; [tp-nickname tp-id]
   [ipv4-config tp-id]
   [tp-status tp-id]
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

(defn main-panel []
  [:div
   #_#_[:p [:button {:on-click #(rf/dispatch [:mqtt/connect])} "Connect MQTT"]]
   [:p [:button {:on-click #(rf/dispatch [:mqtt/publish! "World" "Hello World!"])} "Hello World!"]]
   [:div {:style (:teleporters-container styles)}
    [tp-panel "tp1"]
    [tp-panel "tp2"]
    ]
   [:div {:style {:display :flex
                  :flex-direction :column
                  :align-items :center
                  :margin-top "1rem"}}
    [:> ButtonGroup
     [:> Button {:color "primary"} "Start session"]
     [:> Button {:color "secondary"} "Stop session"]]]
   ])
