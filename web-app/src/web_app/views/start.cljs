(ns web-app.views.start
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.components.icon :refer [logo+slogan]]))

(defn index [_]
  [:div.start.squeeze
   [logo+slogan]
   [:> Button
    {:type "primary"
     :on-click #(rfe/push-state :views/login)}
    "Login"]
   [:div.signup
    [:> Button
     {:type "secondary"
      :on-click #(rfe/push-state :views/signup)}
     "Sign up"]]])
