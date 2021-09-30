(ns web-app.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [reitit.frontend.easy :as rfe]
   [web-app.components.mobile-navbar :refer [mobile-navbar]]
   ["antd" :refer [Layout Layout.Sider Layout.Content Layout.Footer Menu Menu.Item]]
   ["@ant-design/icons" :refer [ControlOutlined UnorderedListOutlined]]
   ))

(defonce match (r/atom nil))

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [:> Layout.Sider {:breakpoint "lg"
                      :collapsed-width "0"}
     [:> Menu {:theme "dark" :mode "inline"}
      [:> Menu.Item {:key "1" :on-click #(rfe/push-state :views/session)} "Session"]
      [:> Menu.Item {:key "2" :on-click #(rfe/push-state :views/teleporters)} "Teleporters"]]
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
       [mobile-navbar {:navbar-items [{:title "Session"
                                       :icon-component ControlOutlined
                                       :on-click #(rfe/push-state :views/session)
                                       :active? (= name :views/session)}
                                      {:title "Teleporters"
                                       :icon-component UnorderedListOutlined
                                       :on-click #(rfe/push-state :views/teleporters)
                                       :active? (= name :views/teleporters)}
                                      ]}]))
     ]]])


