(ns web-app.views.teleporter.paired
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))


(defn index []
  (r/with-let [teleporter (rf/subscribe [:teleporter/teleporter])
               paired? (rf/subscribe [:teleporter/paired?])]
    (if @paired?
      [:div.paired.squeeze.success
       [:img {:on-click #(rfe/push-state :views/room)
              :src "/img/checkmark.png"}]
       [:div.connected "Connected to"]
       [:div.nickname (:teleporter/nickname @teleporter)]]

      [:div.paired.squeeze
       [:div.intro
        [:div.title "Your Teleporter unpaired itself"]]
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views.teleporter/pair)}
        "Redo the linking"]
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views/room)}
        "Continue without"]])))
