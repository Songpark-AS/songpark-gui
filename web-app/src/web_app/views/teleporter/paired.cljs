(ns web-app.views.teleporter.paired
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))


(defn index []
  (r/with-let [teleporter (rf/subscribe [:teleporter/teleporter])
               paired? (rf/subscribe [:teleporter/paired?])]
    (if @paired?
      [:div.paired
       [:img {:on-click #(rfe/push-state :views/home)
              :src "http://foobar.com/img.png"}]
       [:h2 (str "Connected to " (:teleporter/nickname @teleporter))]]
      [:div.paired
       [:p "Your Teleporter unpaired itself."]
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views.teleporter/pair)}
        "Redo the pairing"]
       [:> Button
        {:type "primary"
         :on-click #(rfe/push-state :views/home)}
        "Continue without pairing"]])))
