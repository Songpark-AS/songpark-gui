(ns web-app.event.pairing
  (:require [re-frame.core :as rf]))


(rf/reg-event-fx
 :pairing/paired
 (fn [{:keys [db]} [_ data]]
   {:dispatch [:app.init/app data]
    :rfe/push-state :views.teleporter/paired}))
