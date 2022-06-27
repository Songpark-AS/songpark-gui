(ns web-app.views.teleporter
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.views.teleporter.details :as teleporter.details]))

(defn index []
  (r/with-let [paired? (rf/subscribe [:teleporter/paired?])]
    [:div.teleporter
     (if-not @paired?
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views.teleporter/pair)}
        "You are not paired. Pair a teleporter?"]
       [teleporter.details/index])]))
