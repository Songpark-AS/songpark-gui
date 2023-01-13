(ns web-app.views.topbar
  (:require ["antd" :refer [Button]]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.components.icon :as icon]))


(defn- show-profile [profile]
  (let [{:profile/keys [name position image-url]} @profile]
    [:div.profile
     {:on-click #(rfe/push-state :views/profile)}
     [:div.account
      (if-not (str/blank? image-url)
        [:img {:src image-url}]
        [icon/account])]
     [:div.details
      [:span.name name]
      [:span.position position]]]))

(defn- show-teleporter-status [teleporter]
  (r/with-let [paired? (rf/subscribe [:teleporter/paired?])]
    [:div.status
     {:on-click #(rfe/push-state :views/teleporter)}
     (if @paired?
       [:span.linked "Linked"]
       [:span.not-linked "Not linked"])]))

(defn index []
  (r/with-let [profile (rf/subscribe [:profile/profile])]
    [:div.topbar
     [show-profile profile]
     [show-teleporter-status nil]]))
