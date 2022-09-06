(ns web-app.views.room-host
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [tick.core :as t]
            [tick.alpha.interval :as t.i]
            [web-app.components.icon :refer [add]]))

(defn- get-compareable-days [inst]
  (let [inst-year (t/int (t/year inst))
        inst-month (t/int (t/month inst))
        inst-day (t/day-of-month inst)]
    (+ inst-year inst-month inst-day)))

(defn- get-time-display [days-today last-jammed]
  (let [days-jammed (get-compareable-days last-jammed)]
    (cond (= days-today
             days-jammed)
          "today"
          (= (dec days-today)
             days-jammed)
          "yesterday"
          :else
          (str (- days-today days-jammed) " days ago"))))

(defn show-rooms [rooms]
  (let [data @rooms
        days-today (get-compareable-days (t/now))]
    [:div.rooms
     (for [{:room/keys [id name last-jammers last-jammed]} data]
       ^{:key [:room/id id]}
       [:div.room-entry
        {:on-click #(rf/dispatch [:room.session/host id])}
        [:div.left
         [:div.room-name name]
         [:div.jammers (str/join ", " last-jammers)]]
        [:div.right
         (when last-jammed
           (get-time-display days-today last-jammed))]])]))

(defn index []
  (let [rooms (rf/subscribe [:room/room])]
    [:div.room-host
     [show-rooms rooms]
     [:div.create-room
      {:on-click #(rfe/push-state :views.room/create)}
      [add]
      "Create new room"]]))
