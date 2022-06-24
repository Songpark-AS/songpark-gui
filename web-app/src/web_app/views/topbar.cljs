(ns web-app.views.topbar
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
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
  (r/with-let [paired? (rf/subscribe [:teleporter/paired?])]
    [:div.status
     {:on-click #(rfe/push-state :views/teleporter)}
     (if @paired?
       "Linked"
       "Not linked")]))

(defn index []
  (r/with-let [profile (rf/subscribe [:profile/profile])]
    [:div.topbar
     [show-profile profile]
     [show-teleporter-status nil]]))
