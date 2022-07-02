(ns web-app.components.jammer
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.icon :as icon]
            [web-app.components.knob :refer [knob]]))


(defn show-latency [jam]
  [:div.latency (str (:latency @jam) "ms latency")])

(defn show-jammer [jammer jam]
  (let [{:profile/keys [name position image-url]} @jammer]
    [:div.profile
     {:key [:jammer name]}
     [:div.picture
      (if-not (str/blank? image-url)
        [:img {:src image-url}]
        [icon/account])]
     [:div.details
      [:div.name name]
      [:div.position position]
      [show-latency jam]]]))

(defn muted [jammer mute-event]
  (if (:jammer/muted? @jammer)
    [:div.muted
     {:on-click mute-event}
     [:div.blank]
     [:div.text
      [:h2 "MUTED"]]]
    nil))

(defn jammer [{:keys [jammer]}]
  (r/with-let [jam (rf/subscribe [:teleporter/coredump])
               volume (rf/subscribe [:teleporter/setting nil :volume/network-volume])
               mute-event (fn [_]
                            (rf/dispatch [:room/jammer
                                          (:auth.user/id @jammer)
                                          :jammer/muted?
                                          (not (:jammer/muted? @jammer))]))]
    [:div.jammer
     [:div.click-area
      {:on-click mute-event}]
     [muted jammer mute-event]
     [show-jammer jammer jam]
     [knob {:skin "light"
            :model volume
            :on-change #(rf/dispatch [:teleporter/setting
                                      nil
                                      :volume/network-volume
                                      %
                                      {:message/type :teleporter.cmd/network-volume
                                       :teleporter/volume %}])}]]))
