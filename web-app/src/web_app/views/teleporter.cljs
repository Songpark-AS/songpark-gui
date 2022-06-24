(ns web-app.views.teleporter
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))

(defn index []
  (r/with-let [teleporter (rf/subscribe [:teleporter/teleporter])
               paired? (rf/subscribe [:teleporter/paired?])]
    [:div.teleporter
     (if-not @paired?
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views.teleporter/pair)}
        "You are not paired. Pair a teleporter?"]
       (let [{:keys [teleporter/nickname]} @teleporter]
         [:div.paired
          [:p "You are currently paired with " nickname  "."]
          [:> Button
           {:type "primary"
            :on-click #(rf/dispatch [:teleporter/unpair])}
           "Unpair"]]))]))
