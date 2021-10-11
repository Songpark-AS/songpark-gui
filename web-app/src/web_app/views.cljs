(ns web-app.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [reitit.frontend.easy :as rfe]
   [web-app.components.mobile-navbar :refer [mobile-navbar]]
   ["antd" :refer [Layout Layout.Sider Layout.Content Layout.Footer Menu Menu.Item]]
   ["@ant-design/icons" :refer [ControlOutlined ControlFilled UnorderedListOutlined]]
   ))

(defonce match (r/atom nil))

(defn JamIcon []
  [:> ControlOutlined])
(defn TeleportersIcon []
  [:> UnorderedListOutlined])

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [:> Layout.Sider {:breakpoint "lg"
                      :collapsed-width "0"}
     [:> Menu {:theme "dark" :mode "inline" :selected-keys (:name (:data @match))}
      [:> Menu.Item {:key :views/jam :icon (r/as-element [JamIcon]) :on-click #(rfe/push-state :views/jam)} "Jam"]
      [:> Menu.Item {:key :views/teleporters :icon (r/as-element [TeleportersIcon]) :on-click #(rfe/push-state :views/teleporters)} "Teleporters"]]
     ]
    [:> Layout
     [:> Layout.Content
      [:div.content-wrapper
       (if @match
         (let [view (:view (:data @match))]
           [view @match])
         )]]
     (if @match
       (let [name (:name (:data @match))]
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
                                      ]}]))
     ]]])


