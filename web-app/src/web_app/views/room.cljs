(ns web-app.views.room
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))

(defn index []
  [:div.room
   [:> Button
    {:on-click #(rfe/push-state :views.room/host)}
    "Host room"]
   [:> Button
    {:on-click #(rfe/push-state :views.room/join)}
    "Join room"]])
