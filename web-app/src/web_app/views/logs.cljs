(ns web-app.views.logs
  (:require ["antd" :refer [Button
                            Radio Radio.Group
                            Tabs Tabs.TabPane]]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]))

(defn- pretty-level [level]
  (when level
    (-> level name str/upper-case)))

(def styles {:log {:margin-bottom "0.5em"
                   :padding-bottom "0.5em"
                   :border-bottom "1px dotted grey"}
             :level {}
             :info {:text-transform "uppercase"
                    :padding "0.2em"
                    :background-color "lightblue"}
             :error {:text-transform "uppercase"
                     :padding "0.2em"
                     :background-color "#800000"
                     :color "#f0f0f0"}
             :warn {:text-transform "uppercase"
                    :padding "0.2em"
                    :background-color "orange"
                    :color "#f0f0f0"}
             :debug {:text-transform "uppercase"
                     :padding "0.2em"
                     :background-color "purple"
                     :color "#f0f0f0"}
             :id {:margin "0 1em"
                  :background-color "grey"
                  :color "#f0f0f0"
                  :display "inline-block"}})


(defn show-log [{:keys [log/data log/level message/id]}]
  [:div.log {:style (:log styles)}
   [:span {:style (styles level)} level]
   [:span {:style (styles :id)} id]
   [:span data]])

(defn show-logs [selected-teleporter selected-level]
  (let [level @selected-level
        teleporter-id @selected-teleporter
        logs @(rf/subscribe [:teleporter/log teleporter-id level])]
    [:div
     [:> Button {:style {:margin-bottom "2em"}
                 :type "danger"
                 :on-click #(rf/dispatch [:teleporter.log/clear! teleporter-id level])}
      (str "Clear " (pretty-level level))]
     [:<>
      [:div.log {:style (:log styles)}
       [:span {:style (styles level)} "Level"]
       [:span {:style (styles :id)} "Message ID (internal)"]
       [:span "Log"]]
      (for [log logs]
        ^{:key [teleporter-id (:log/level log) (:message/id log)]}
        [show-log log])]]))


(defn index []
  (r/with-let [teleporters (rf/subscribe [:teleporters])
               selected-teleporter (rf/subscribe [:view.log/teleporter])
               selected-level (rf/subscribe [:view.log/level])]
    (let [default-key (if @selected-level
                        (-> @selected-level name str/upper-case)
                        "INFO")]
      [:div
       [:h1 "Logs"]
       [:h2 "Teleporters"]
       [:div
        [:> Radio.Group {:value (str @selected-teleporter)}
         [:<>
          (for [{:teleporter/keys [uuid nickname]} (sort-by :teleporter/nickname @teleporters)]
            [:> Radio {:value (str uuid)
                       :key [:teleporter uuid]
                       :on-click #(rf/dispatch [:view.log/teleporter uuid])}
             nickname])]]]
       [:> Button {:style {:margin-bottom "1em"
                           :margin-top "1em"}
                   :type "danger"
                   :on-click #(rf/dispatch [:teleporter.log/clear! @selected-teleporter])}
        (str "Clear all logs")]
       [:div
        [:> Tabs {:on-change #(rf/dispatch [:view.log/level (-> % str/lower-case keyword)])
                  :default-active-key default-key}
         [:> Tabs.TabPane {:tab "INFO"
                           :key "INFO"}]
         [:> Tabs.TabPane {:tab "ERROR"
                           :key "ERROR"}]
         [:> Tabs.TabPane {:tab "WARN"
                           :key "WARN"}]
         [:> Tabs.TabPane {:tab "DEBUG"
                           :key "DEBUG"}]]]
       [show-logs selected-teleporter selected-level]])))
