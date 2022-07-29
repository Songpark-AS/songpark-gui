(ns web-app.components.preset
  (:refer-clojure :exclude [sort])
  (:require ["antd" :refer [Button
                            Input]]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web-app.components.icon :refer [add
                                             delete
                                             sort]]))


(defn show-preset [active? input {:keys [fx.preset/name fx.preset/id fx/fxs]}]
  (let [types (->> fxs
                   (map (comp clojure.core/name :fx/type))
                   clojure.core/sort
                   (str/join ", "))]
    [:div.preset
     {:key [::preset id]}
     [:div
      [:div.title
       {:on-click #(do (rf/dispatch [:fx.preset/set input id])
                       (reset! active? false))}
       name]
      [:div.fxs types]]
     (if id
       [:div.delete
        {:on-click #(rf/dispatch [:fx.preset/delete id])}
        [delete]])]))

(defn save-preset [input adding?]
  (r/with-let [preset-name (r/atom "")]
    [:div.save-form
     [:div.navigation
      {:on-click #(reset! adding? false)}
      [:span.material-symbols-outlined "arrow_right_alt"]
      [:div "Add current FX as preset"]]
     [:div.form
      [:> Input
       {:default-value @preset-name
        :placeholder "Name"
        :on-change #(reset! preset-name (-> % .-target .-value))}]
      [:> Button
       {:type "primary"
        :on-click #(rf/dispatch [:fx.preset/save input @preset-name adding?])}
       "Save"]]]))

(defn preset [{:keys [input] :as props}]
  (r/with-let [active? (r/atom false)
               adding? (r/atom false)
               presets (rf/subscribe [:fx/presets])
               current-preset (rf/subscribe [:fx.preset/current input])
               changed? (rf/subscribe [:teleporter/setting nil :fx.preset/changed?])]
    [:div.preset
     {:class (if @active?
               "active")}
     ;; info bar
     [:div.info
      [:div.left
       (if-let [current @current-preset]
         [:<>
          [:span.current "Preset"]
          [:span.name (:fx.preset/name current)]]
         [:span "No preset is currently active"])]
      (if @changed?
        [:div.save
         {:on-click #(rf/dispatch [:fx.preset/update input (:fx.preset/id @current-preset)])}
         "Save"])
      [:div.change
       {:on-click #(reset! active? true)}
       [sort]]]

     ;; body
     [:div.body
      (if (false? @adding?)
        [:<>
         [:div.navigation
          {:on-click #(reset! active? false)}
          [:span.material-symbols-outlined "arrow_right_alt"]
          [:div "Effects preset"]]
         ;; show presets
         [:<>
          ;; [:h2.system-effects "System effects"]
          ;; (map #(show-preset active? input %) system-effects)
          ;; [:h2.user-effects "User effects"]
          (map #(show-preset active? input %) @presets)]
         ;; add a new preset (button)
         [:div.add
          {:on-click #(reset! adding? true)}
          [add]
          [:div "New preset"]]]
        [save-preset input adding?])]]))
