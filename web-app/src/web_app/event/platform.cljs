(ns web-app.event.platform
  (:require [re-frame.core :as rf]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :fetch-platform-version
 (fn [_ _]
   {:dispatch [:http/get (get-platform-url "/version") nil :set-platform-version]}))

(rf/reg-event-fx
 :fetch-latest-available-apt-version
 (fn [_ _]
   {:dispatch [:http/get (get-platform-url "/latest-available-version") nil :set-latest-available-apt-version]}))

(rf/reg-event-db
 :set-platform-version
 (fn [db [_ {:keys [version]}]]
   (assoc db :platform/version version)))

(rf/reg-event-db
 :set-latest-available-apt-version
 (fn [db [_ {:keys [version]}]]
   (assoc db :teleporter/latest-available-apt-version version)))
