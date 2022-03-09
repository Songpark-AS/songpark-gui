(ns web-app.subs.platform
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :platform/version
 (fn [db _]
   (:platform/version db)))


