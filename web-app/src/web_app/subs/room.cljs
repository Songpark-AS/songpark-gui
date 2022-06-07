(ns web-app.subs.room
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :room/room
 (fn [db _]
   (:room/room db)))
