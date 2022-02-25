(ns web-app.views
  (:require
   ["antd" :refer [Layout Layout.Sider Layout.Content Layout.Footer Menu Menu.Item]]
   ["@ant-design/icons" :refer [ControlOutlined ControlFilled UnorderedListOutlined CopyOutlined RocketFilled]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   [taoensso.timbre :as log]
   [web-app.components.mobile-navbar :refer [mobile-navbar]]))

(defonce match (r/atom nil))

(defn JamIcon []
  [:> ControlOutlined])
(defn TeleportersIcon []
  [:> UnorderedListOutlined])
(defn TelemetryIcon []
  [:> CopyOutlined])
(defn DevIcon []
  [:> RocketFilled])

(defn menu []
  [:> Layout.Sider {:breakpoint "lg"
                    :collapsed-width "0"}
   [:> Menu {:theme "dark" :mode "inline" :selected-keys (:name (:data @match))}
    [:> Menu.Item {:key :views/jam :icon (r/as-element [JamIcon]) :on-click #(rfe/push-state :views/jam)} "Jam"]
    [:> Menu.Item {:key :views/teleporters :icon (r/as-element [TeleportersIcon]) :on-click #(rfe/push-state :views/teleporters)} "Teleporters"]
    [:> Menu.Item {:key :views/telemetry :icon (r/as-element [TelemetryIcon]) :on-click #(rfe/push-state :views/telemetry)} "Telemetry"]
    [:> Menu.Item {:key :views/dev :icon (r/as-element [DevIcon]) :on-click #(rfe/push-state :views/dev)} "Dev"]
    ]])

(defn main []
  [:> Layout
   [:> Layout.Content
    [:div.content-wrapper
     (if @match
       (let [view (:view (:data @match))]
         [view @match]))]]
   (if @match
     (let [name (:name (:data @match))]
       (when-not (= name :views/dev) 
         [mobile-navbar {:navbar-items [{:title "Jam"
                                         :icon-component (if (= name :views/jam)
                                                           ControlFilled
                                                           ControlOutlined)
                                         :on-click #(rfe/push-state :views/jam)
                                         :active? (= name :views/jam)}
                                        {:title "Teleporters"
                                         :icon-component UnorderedListOutlined
                                         :on-click #(rfe/push-state :views/teleporters)
                                         :active? (= name :views/teleporters)}
                                        {:title "Telemetry"
                                         :icon-component CopyOutlined
                                         :on-click #(rfe/push-state :views/telemetry)
                                         :active? (= name :views/telemetry)}
                                        {:title "Dev"
                                         :icon-component RocketFilled
                                         :on-click #(rfe/push-state :views/dev)
                                         :active? (= name :views/dev)}]}])))])

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [menu]
    [main]]])


