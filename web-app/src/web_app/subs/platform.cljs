(ns web-app.subs.platform
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :platform/version
 (fn [db _]
   (:platform/version db)))

(rf/reg-sub
 :platform/latest-available-apt-version
 (fn [db _]
   (:platform/latest-available-apt-version db)))


