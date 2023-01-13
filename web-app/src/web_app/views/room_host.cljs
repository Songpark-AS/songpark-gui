(ns web-app.views.room-host
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [tick.core :as t]
            [web-app.components.icon :refer [add]]))


(defn leap-year? [year]
  (cond (and (zero? (mod year 4))
             (or (zero? (mod year 400))
                 (not (zero? (mod year 100)))))
        true
        :else
        false))

(def days-passed-per-month
  {1 31
   3 (+ 31 28 31)
   4 (+ 30 31 28 31)
   5 (+ 31 30 31 28 31)
   6 (+ 30 31 30 31 28 31)
   7 (+ 31 30 31 30 31 28 31)
   8 (+ 31 31 30 31 30 31 28 31)
   9 (+ 30 31 31 30 31 30 31 28 31)
   10 (+ 31 30 31 31 30 31 30 31 28 31)
   11 (+ 30 31 30 31 31 30 31 30 31 28 31)
   12 (+ 31 30 31 30 31 31 30 31 30 31 28 31)})

(defn get-month-days [leap? number-month]
  (cond (and leap?
             (= number-month 2))
        (+ 31 29)

        (= number-month 2)
        (+ 31 28)
        :else
        (get days-passed-per-month number-month)))

(defn get-compareable-days [inst]
  (let [number-year (t/int (t/year inst))
        days-year (* 365 number-year)
        days-month (get-month-days (leap-year? number-year) (t/int (t/month inst)))
        days-day (t/day-of-month inst)]
    (+ days-year days-month days-day)))

(defn get-time-display [days-today last-jammed]
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
        {:on-click #(rf/dispatch [:room.jam/host id])}
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
