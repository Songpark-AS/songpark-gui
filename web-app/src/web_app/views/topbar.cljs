(ns web-app.views.topbar
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]))


(defn- show-profile [profile]
  (let [{:profile/keys [name position image-url]} @profile]
    [:div.profile
     {:on-click #(rfe/push-state :views/profile)}
     [:img {:src image-url}]
     [:span.name name]
     [:span.position position]]))

(defn- show-teleporter-status [teleporter]
  "TELEPORTER STATUS")

(defn index []
  (r/with-let [profile (rf/subscribe [:profile/profile])]
    [:div.topbar
     [show-profile profile]
     [show-teleporter-status nil]]))
