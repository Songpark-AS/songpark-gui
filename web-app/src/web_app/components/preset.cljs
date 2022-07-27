(ns web-app.components.preset
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web-app.components.icon :refer [add
                                             delete]]))


(defn show-preset [active? input {:keys [fx.preset/name fx.preset/id fx/fxs]}]
  (let [types (->> fxs
                   (map (comp clojure.core/name :fx/type))
                   sort
                   (str/join ", "))]
    [:div {:key [::preset id]}
     [:div.title
      {:on-click #(do (rf/dispatch [:fx.preset/set input id])
                      (reset! active? false))}
      name]
     [:div.fxs types]
     (if id
       [:div.delete
        {:on-click #(rf/dispatch [:fx.preset/delete id])}
        [delete]])]))

(defn preset [{:keys [input] :as props}]
  (r/with-let [active? (r/atom false)
               adding? (r/atom false)
               presets (rf/subscribe [:fx/presets])
               current-preset (rf/subscribe [:fx.preset/current])
               changed? (rf/subscribe [:teleporter/setting nil :fx.preset/changed?])]
    [:div.preset
     {:class (if @active?
               "active")}
     ;; info bar
     [:div.info
      [:div.left
       (if-let [current @current-preset]
         [:<>
          [:span.current "Preset active"]
          [:span.name (:fx.preset/name current)]]
         [:span "No preset is currently active"])]
      (if @changed?
        [:div.save
         {:on-click #(rf/dispatch [:fx.preset/update input (:fx.preset/id @current-preset)])}
         "Save"])
      [:div.change
       {:on-click #(reset! active? true)}
       "List"]]

     ;; body
     [:div.body
      (if (false? @adding?)
        [:<>
         [:span.material-symbols-outlined
          {:on-click #(reset! active? false)}
          "arrow_right_alt"]
         ;; show presets
         [:<>
          (map #(show-preset active? input %) @presets)]
         ;; add a new preset (button)
         [:div.add
          {:on-click #(reset! adding? true)}
          [add] "New preset"]]
        [:div.preset-form
         "foobar"])]]))
