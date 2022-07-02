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
               linked? (r/atom false)
               master-volume (rf/subscribe [:teleporter/setting
                                            nil
                                            :volume/global-volume])
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
        :linked? linked?
        :knob1 {:title "INPUT 1"
                :on-change #(if @linked?
                              (do
                                (rf/dispatch [:teleporter/setting
                                              nil
                                              :volume/input1-volume
                                              %
                                              {:message/type :teleporter.cmd/input1+2-volume
                                               :teleporter/volume %}])
                                (rf/dispatch [:teleporter/setting
                                              nil
                                              :volume/input2-volume
                                              %]))
                              (rf/dispatch [:teleporter/setting
                                            nil
                                            :volume/input1-volume
                                            %
                                            {:message/type :teleporter.cmd/input1-volume
                                             :teleporter/volume %}]))
                :model (rf/subscribe [:teleporter/setting
                                      nil
                                      :volume/input1-volume])}
        :knob2 {:title "INPUT 2"
                :on-change #(rf/dispatch [:teleporter/setting
                                          nil
                                          :volume/input2-volume
                                          %
                                          {:message/type :teleporter.cmd/input2-volume
                                           :teleporter/volume %}])
                :model (rf/subscribe [:teleporter/setting
                                      nil
                                      :volume/input2-volume])}}]]
     [:div.jammers
      [:<>
       (for [id @jammers]
         ^{:key [:jammer id]}
         [jammer {:jammer (rf/subscribe [:room/jammer id])}])]]]))
