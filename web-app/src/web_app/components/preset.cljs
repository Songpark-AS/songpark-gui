(ns web-app.components.preset
  (:require [re-frame.core :as rf]
            [reagent.core :as r]))

(defn preset [props]
  (r/with-let [active? (r/atom false)
               current-preset (rf/subscribe [:teleporter/setting nil :fx.preset/current])
               changed? (rf/subscribe [:teleporter/setting nil :fx.preset/changed?])]
    [:div.preset
     {:class (if @active?
               "active")}
     [:div.info
      [:div.left
       [:span.current "Preset active"]
       [:span.name (:preset/name @current-preset)]]
      (if @changed?
        [:div.save
         "Save"])
      [:div.change
       {:on-click #(reset! active? true)}
       "List"]]
     [:div.body
      [:span.material-symbols-outlined
       {:on-click #(reset! active? false)}
       "arrow_right_alt"]
      [:<>
       (repeat 15 [:div "my body"])]]]))
