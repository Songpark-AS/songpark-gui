(ns web-app.views.levels
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.jammer :refer [jammer]]
            [web-app.components.knob :refer [knob
                                             knob-duo]]))

(rf/reg-event-db
 :knob/volume
 (fn [db [_ id v]]
   (assoc-in db [:knob/volume id] v)))

(rf/reg-sub
 :knob/volume
 (fn [db [_ id]]
   (get-in db [:knob/volume id] 0)))

(defn index []
  (r/with-let [m (r/atom 0)
               master-volume (rf/subscribe [:teleporter/setting nil :volume/global-volume])
               jammers (rf/subscribe [:room/jammers])]
    [:div.levels
     [:div.controls
      [knob
       {:title "MASTER"
        :on-change #(rf/dispatch [:teleporter/setting
                                  nil
                                  :volume/global-volume
                                  %
                                  {:message/type :teleporter.cmd/global-volume
                                   :teleporter/volume %}])
        :model master-volume}]
      [knob-duo
       {:skin "dark"
        :knob1 {:title "INPUT 1"

                :on-change #(rf/dispatch [:knob/volume :input1 %])
                :model (rf/subscribe [:knob/volume :input1])}
        :knob2 {:title "INPUT 2"
                :on-change #(rf/dispatch [:knob/volume :input2 %])
                :model (rf/subscribe [:knob/volume :input2])}}]]
     [:div.jammers
      [:<>
       (for [id @jammers]
         [jammer {:jammer (rf/subscribe [:room/jammer id])}])]]]))
