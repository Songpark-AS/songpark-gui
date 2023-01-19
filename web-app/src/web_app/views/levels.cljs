(ns web-app.views.levels
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.jammer :refer [jammer]]
            [web-app.components.knob :refer [knob
                                             knob-duo]]))

(defn index []
  (r/with-let [linked? (rf/subscribe [:teleporter/setting nil :knob.duo/linked? false])
               master-volume (rf/subscribe [:teleporter/setting
                                            nil
                                            :volume/global-volume])
               jammers (rf/subscribe [:room/people :other-jammers])]
    [:div.levels
     [:div.controls
      [knob-duo
       {:skin "dark"
        :linked? linked?
        :linked-change #(rf/dispatch [:teleporter/setting nil :knob.duo/linked? %])
        :knob1 {:title "INPUT 1"
                :value/max 20
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
                :value/max 20
                :on-change #(rf/dispatch [:teleporter/setting
                                          nil
                                          :volume/input2-volume
                                          %
                                          {:message/type :teleporter.cmd/input2-volume
                                           :teleporter/volume %}])
                :model (rf/subscribe [:teleporter/setting
                                      nil
                                      :volume/input2-volume])}}]
      [knob
       {:title "MASTER"
        :value/max 20
        :on-change #(rf/dispatch [:teleporter/setting
                                  nil
                                  :volume/global-volume
                                  %
                                  {:message/type :teleporter.cmd/global-volume
                                   :teleporter/volume %}])
        :model master-volume}]]
     [:div.jammers
      [:<>
       (for [{:keys [auth.user/id]} @jammers]
         ^{:key [:jammer id]}
         [jammer {:jammer (rf/subscribe [:room/jammer id])}])]]]))
