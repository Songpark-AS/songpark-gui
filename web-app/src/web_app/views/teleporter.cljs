(ns web-app.views.teleporter
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.history :refer [back]]
            [web-app.views.teleporter.details :as teleporter.details]))

(defn index []
  (r/with-let [paired? (rf/subscribe [:teleporter/paired?])]
    [:div.teleporter
     (if-not @paired?
       [:div.teleporter.squeeze
        [:div.intro
         [:div.title "You are not linked"]
         [:div.slogan "Link a teleporter?"]]
        [:div.back
         {:on-click #(back)}
         "Go back"]
        [:> Button
         {:type "primary"
          :on-click #(rfe/push-state :views.teleporter/pair)}
         "Link"]]
       [teleporter.details/index])]))
