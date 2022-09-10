(ns web-app.views.footer
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.components.icon :as icon]))


(defn- get-active-css-class [view-names current]
  (if (view-names current)
    "active"
    ""))

(defn show-room [view-name]
  (r/with-let [jamming? (rf/subscribe [:room/jamming?])]
    [:div.room
     {:class (get-active-css-class #{:views/room
                                     :views.room/create
                                     :views.room/jam
                                     :views.room/host
                                     :views.room/join} view-name)
      :on-click #(if @jamming?
                   (rfe/push-state :views.room/jam)
                   (rfe/push-state :views/room))}
     [icon/room]
     "Room"]))

(defn show-levels [view-name]
  [:div.levels
   {:class (get-active-css-class #{:views/levels} view-name)
    :on-click #(rfe/push-state :views/levels)}
   [icon/levels]
   "Levels"])

(defn show-input1 [view-name]
  [:div.input1
   {:class (get-active-css-class #{:views/input1} view-name)
    :on-click #(rfe/push-state :views/input1)}
   [icon/input]
   "Input 1"])

(defn show-input2 [view-name]
  [:div.input2
   {:class (get-active-css-class #{:views/input2} view-name)
    :on-click #(rfe/push-state :views/input2)}
   [icon/input]
   "Input 2"])

(defn index [view-name]
  [:footer
   [show-room view-name]
   [show-levels view-name]
   [show-input1 view-name]
   [show-input2 view-name]])
