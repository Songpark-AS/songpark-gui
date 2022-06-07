(ns web-app.views.footer
  (:require [reitit.frontend.easy :as rfe]))


(defn- get-active-css-class [view-name]
  (if (#{:views/room
         :views/levels
         :views/input1
         :views/input2} view-name)
    "active"
    ""))

(defn show-room [view-name]
  [:div.room
   {:class (get-active-css-class view-name)
    :on-click #(rfe/push-state :views/room)}
   "room"])

(defn show-levels [view-name]
  [:div.levels
   {:class (get-active-css-class view-name)
    :on-click #(rfe/push-state :views/levels)}
   "levels"])

(defn show-input1 [view-name]
  [:div.input1
   {:class (get-active-css-class view-name)
    :on-click #(rfe/push-state :views/input1)}
   "input1"])

(defn show-input2 [view-name]
  [:div.input2
   {:class (get-active-css-class view-name)
    :on-click #(rfe/push-state :views/input2)}
   "input2"])

(defn index [{view-name :name :as _match}]
  [:footer
   [show-room view-name]
   [show-levels view-name]
   [show-input1 view-name]
   [show-input2 view-name]])
