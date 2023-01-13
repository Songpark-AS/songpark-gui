(ns web-app.subs.navigation
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :navigation/current
 (fn [db _]
   (first (get db :navigation/routes))))

(rf/reg-sub
 :navigation/previous
 (fn [db _]
   (second (get db :navigation/routes))))

(rf/reg-sub
 :navigation.room/last-known
 (fn [db _]
   (get db :navigation.room/last-known)))
