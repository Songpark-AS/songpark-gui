(ns web-app.views.telemetry
  (:require ["antd" :refer [Button
                            Radio Radio.Group
                            Table
                            Tabs Tabs.TabPane]]
            [clojure.string :as str]
            [songpark.common.config :refer [config]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]))

(defn- pretty-level [level]
  (when level
    (-> level name str/upper-case)))

(def styles {:log {:margin-bottom "0.5em"
                   :padding-bottom "0.5em"
                   :border-bottom "1px dotted grey"}
             :table {:padding "0.2em"
                     :margin-bottom "3em"}
             :table/th {:text-align "left"
                        :padding "0.2em 5em 0.2em 0"}
             :table/td {:text-align "left"}
             :h1 {:font-size "2em"}
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

(defn show-selected-logs [selected-teleporter selected-level]
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

(defn show-logs [teleporters selected-teleporter selected-level]
  (let [default-key (if @selected-level
                      (-> @selected-level name str/upper-case)
                      "INFO")]
    [:<>
     [:h2 "Teleporters"]
     [:div
      [:> Radio.Group {:value (str @selected-teleporter)}
       [:<>
        (for [{:teleporter/keys [uuid nickname]} (sort-by :teleporter/nickname teleporters)]
          [:> Radio {:value (str uuid)
                     :key [:teleporter uuid]
                     :on-click #(rf/dispatch [:view.telemetry.log/teleporter uuid])}
           nickname])]]]
     [:> Button {:style {:margin-bottom "1em"
                         :margin-top "1em"}
                 :type "danger"
                 :on-click #(rf/dispatch [:teleporter.log/clear! @selected-teleporter])}
      (str "Clear all logs")]
     [:div
      [:> Tabs {:on-change #(rf/dispatch [:view.telemetry.log/level (-> % str/lower-case keyword)])
                :default-active-key default-key}
       [:> Tabs.TabPane {:tab "INFO"
                         :key "INFO"}]
       [:> Tabs.TabPane {:tab "ERROR"
                         :key "ERROR"}]
       [:> Tabs.TabPane {:tab "WARN"
                         :key "WARN"}]
       [:> Tabs.TabPane {:tab "DEBUG"
                         :key "DEBUG"}]]]
     [show-selected-logs selected-teleporter selected-level]]))

(defn- show-versions [teleporters]
  (let [platform-version @(rf/subscribe [:platform-version])]
    [:<>
     [:h2 "Versions"]
     [:table {:style (:table styles)}
      [:tbody
       [:tr
        [:th {:style (:table/th styles)} "What"]
        [:th {:style (:table/th styles)} "Version"]]
       
       (for [[what version] [["App" (:version @config)]
                             ["Platform" platform-version]]]
         ^{:key [what version]}
         [:tr
          [:th {:style (:table/th styles)} what]
          [:td {:style (:table/td styles)} version]])]]
     [:h2 "Teleporters"]
     [:table {:style (:table styles)}
      [:tbody
       [:tr
        (for [name ["Name" "Version"]]
          ^{:key name}
          [:th {:style (:table/th styles)} name])]
       (for [{:teleporter/keys [nickname id apt-version]} (into [] (vals @teleporters))]
         ^{:key [:teleporter id]}
         [:tr
          [:th {:style (:table/th styles)} nickname]
          [:td {:style (:table/td styles)} apt-version]])]]]))


(defn index []
  (r/with-let [teleporters (rf/subscribe [:teleporters])
               selected-teleporter (rf/subscribe [:view.telemetry.log/teleporter])
               selected-log-level (rf/subscribe [:view.telemetry.log/level])
               selected-tab (rf/subscribe [:view.telemetry/tab])]
    [:div
     [:h1 {:style (:h1 styles)} "Telemetry"]

     [:> Tabs {:on-change #(rf/dispatch [:view.telemetry/tab %])
               :default-active-key @selected-tab}
      [:> Tabs.TabPane {:tab "Versions"
                        :key "VERSIONS"}
       [show-versions teleporters]]
      [:> Tabs.TabPane {:tab "Logs"
                        :key "LOGS"}
       [show-logs teleporters selected-teleporter selected-log-level]]]]))
