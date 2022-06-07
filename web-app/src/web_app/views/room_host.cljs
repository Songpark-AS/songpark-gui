(ns web-app.views.room-host
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))

(defn show-rooms [rooms]
  (let [data @rooms]
    [:ul.rooms
     (for [{:room/keys [id name]} data]
       ^{:key [:room/id id]}
       [:li name])]))

(defn index []
  (let [rooms (rf/subscribe [:room/room])]
    [:div.room-host
     [show-rooms rooms]
     [:div.create-room
      {:on-click #(rfe/push-state :views.room/create)}
      "+ Create new room"]]))
