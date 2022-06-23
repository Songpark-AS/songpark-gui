(ns web-app.views.teleporter
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.teleporter :as teleporter]))

(defn index []
  [:div.teleporter
   (if-not (teleporter/paired?)
     [:> Button
      {:type "primary"
       :on-click #(rfe/push-state :views.teleporter/pair)}
      "You are not paired. Pair a teleporter?"])])
