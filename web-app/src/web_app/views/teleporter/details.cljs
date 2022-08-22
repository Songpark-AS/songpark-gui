(ns web-app.views.teleporter.details
  (:require
   ["antd" :refer [Button
                   Divider
                   Radio.Button
                   Radio.Group]]
   [ez-wire.form :as form]
   [ez-wire.form.helpers :refer [add-external-error
                                 valid?]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [songpark.common.config :refer [config]]
   ["semver" :as semver]
   [web-app.components.icon :refer [arrow-left-alt]]
   [web-app.event.ui]
   [web-app.forms.ipv4 :as ipv4-form]
   [web-app.forms.settings :refer [settingsform]]
   [web-app.history :refer [back]]))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter
(defn wrap-semver-lt [^js semver x y]
  (if-not (or (or (nil? x) (nil? y))
              (or (= x "") (= x "Unknown"))
              (or (= y "") (= y "Unknown")))
    (.lt semver x y)
    false))

(defn- handle-upgrade-failed [tp-id]
  (rf/dispatch [:teleporter/upgrade-status {:teleporter/id tp-id
                                            :teleporter/upgrade-status "failed"}]))

(defn- on-upgrade-click [tp-id]
  ;; start upgrade timeout
  (rf/dispatch [:teleporter/upgrade-timeout
                tp-id
                (js/setTimeout #(handle-upgrade-failed tp-id)
                               (get @config :upgrade_timeout))])

  ;; Send upgrade request to teleporter
  (rf/dispatch [:req-tp-upgrade tp-id])

  ;; We are now upgrading the teleporter
  (rf/dispatch [:teleporter/upgrading? tp-id true]))

(defn- stop-all-streams [tp-id]
  (rf/dispatch [:mqtt/send-message-to-teleporter
                tp-id
                {:message/type :teleporter.cmd/hangup-all
                 :teleporter/id tp-id}]))



(defn settings-form [teleporter]
  (r/with-let [form (settingsform {} teleporter)
               form-data (rf/subscribe [::form/on-valid (:id form)])
               handler (fn [data]
                         (rf/dispatch [:teleporter/setting
                                       (:teleporter/id data)
                                       :teleporter/nickname
                                       (:teleporter/nickname data)]))
               error-handler (fn [{:keys [response]}]
                               (add-external-error form
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:teleporter/settings-form
                                         (merge data
                                                (select-keys teleporter [:teleporter/id]))
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.settings
     [:form
      [form/as-table {} form]]
     [:> Button
      {:type "primary"
       :disabled (not (valid? form-data))
       :on-click event}
       "Save settings"]]))

(defn index []
  (let [teleporter (rf/subscribe [:teleporter/teleporter])]
    ;; grab data
    (rf/dispatch [:teleporter/request-network-config (:teleporter/id @teleporter)])
    (rf/dispatch [:platform/fetch-latest-available-apt-version])
    (fn []
      (let [{:teleporter/keys [id nickname apt-version]} @teleporter
            network-config (rf/subscribe [:teleporter/net-config id])
            network-choice (rf/subscribe [:component/radio-group :ipv4 :dhcp])
            apt-version-from-mqtt (rf/subscribe [:teleporter/apt-version id])
            upgrading? (rf/subscribe [:teleporter/upgrading? id])

            latest-reported-apt-version (or apt-version-from-mqtt apt-version)
            latest-available-apt-version (rf/subscribe [:teleporter/latest-available-apt-version])]
        [:div.teleporter.details.form
         [:h2
          {:on-click #(back)}
          [arrow-left-alt]
          "Teleporter"]
         ;; Details
         [:> Divider
          {:orientation "left"}
          "Details"]
         [:div.paired
          [:p "You are currently linked with " nickname  "."]
          [:p "Firmware version: " @latest-reported-apt-version]
          [:div
           [:> Button
            {:type "primary"
             :on-click #(rf/dispatch [:teleporter/unpair])}
            "Unlink Teleporter"]]]
         (when (wrap-semver-lt semver
                               @latest-reported-apt-version
                               @latest-available-apt-version)
           [:<>
            [:> Divider
             {:orientation "left"}
             "Firmware"]
            [:p (str "Update available " @latest-available-apt-version)]
            [:> Button {:type "primary"
                        :loading @upgrading?
                        :on-click #(on-upgrade-click id)}
             "Upgrade firmware"]])

         ;; [:> Divider
         ;;  {:orientation "left"}
         ;;  "Commands"]
         ;; [:div.command-buttons
         ;;  [:> Button {:type "danger"
         ;;              :on-click #(stop-all-streams id)} "Stop all streams"]]

         ;; Network
         [:> Divider
          {:orientation "left"}
          "Network settings"]
         (if (not (nil? @network-config))
           [:div.network
            [:> Radio.Group {:value @network-choice
                             :buttonStyle "solid"}
             [:> Radio.Button {:value :manual
                               :on-click #(rf/dispatch [:component/radio-group :ipv4 :manual])}
              "Manual"]
             [:> Radio.Button {:value :dhcp
                               :on-click #(do
                                            (rf/dispatch [:component/radio-group :ipv4 :dhcp])
                                            (when (false? (:ip/dhcp? @network-config))
                                              (rf/dispatch [:teleporter/save-ipv4
                                                            id
                                                            {:ip/dhcp? true}])))}
              "DHCP"]]
            [:div
             (if (= @network-choice :dhcp)
               (let [{:ip/keys [address gateway subnet]}
                     @network-config]
                 [:table.network-details
                  [:tr [:td "IP"] [:td address]]
                  [:tr [:td "Netmask"] [:td subnet]]
                  [:tr [:td "Gateway"] [:td gateway]]])
               (when (not (nil? @network-config))
                 [ipv4-form/ipv4-config id @network-config]))]]
           [:div.network
            [:p "Fetching network configuration"]])

         ;; Settings
         [:> Divider
          {:orientation "left"}
          "Teleporter settings"]
         [settings-form @teleporter]]))))
