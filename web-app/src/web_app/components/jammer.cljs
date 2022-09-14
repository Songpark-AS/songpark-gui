(ns web-app.components.jammer
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.icon :as icon]
            [web-app.components.knob :refer [knob]]))


(defn show-latency [jam]
  [:div.latency (str (get @jam :Latency) "ms latency")])

(defn show-jammer [jammer]
  (let [{:profile/keys [name position image-url]} @jammer]
    [:div.profile
     {:key [:jammer name]}
     [:div.picture
      (if-not (str/blank? image-url)
        [:img {:src image-url}]
        [icon/account])]]))

(defn- show-jammer-details [jammer]
  [:div.details
   [:div.name (:profile/name @jammer)]
   [:div.position (:profile/position @jammer)]])

(defn muted [jammer mute-event]
  (if (:jammer/muted? @jammer)
    [:div.muted
     {:on-click mute-event}
     [:div.blank]
     [:div.text
      [:h2 "MUTED"]]]
    nil))

(defn jammer [{:keys [jammer]}]
  (r/with-let [jam (rf/subscribe [:teleporter/coredump (:teleporter/id jammer)])
               volume (rf/subscribe [:teleporter/setting nil :volume/network-volume])
               playout-delay (rf/subscribe [:teleporter/setting nil :jam/playout-delay])
               mute-event (fn [_]
                            (rf/dispatch [:room/jammer
                                          (:auth.user/id @jammer)
                                          :jammer/muted?
                                          (not (:jammer/muted? @jammer))]))]
    [:div.jammer.component
     [:div.click-area
      {:on-click mute-event}]
     [muted jammer mute-event]
     [:div.upper
      [show-jammer jammer]
      [:div.knobs
       [knob {:skin "light"
              :model playout-delay
              :value/max 24
              :title "Playout Delay"
              :on-change #(rf/dispatch [:teleporter/setting
                                        nil
                                        :jam/playout-delay
                                        %
                                        {:message/type :teleporter.cmd/set-playout-delay
                                         :teleporter/playout-delay %}])}]
       [knob {:skin "light"
              :model volume
              :title "Volume"
              :on-change #(rf/dispatch [:teleporter/setting
                                        nil
                                        :volume/network-volume
                                        %
                                        {:message/type :teleporter.cmd/network-volume
                                         :teleporter/volume %}])}]]]
     [:div.lower
      [show-jammer-details jammer]
      [show-latency jam]]]))
