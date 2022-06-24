(ns web-app.views.teleporter.paired
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))


(defn index []
  (r/with-let [teleporter (rf/subscribe [:teleporter/teleporter])]
    [:div.paired
     [:img {:on-click #(rfe/push-state :views/home)
            :src "http://foobar.com/img.png"}]
       [:h2 (str "Connected to " (:teleporter/nickname @teleporter))]]
    #_[:div.paired "Foo"]))
