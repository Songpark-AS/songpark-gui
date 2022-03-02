(ns web-app.views.teleporter.detail
  (:require
   ["antd" :refer [Button Tabs Tabs.TabPane]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   [web-app.forms.ipv4 :as ipv4-form]
   [version-clj.core :as v]
   [songpark.common.config :refer [config]]
   #_[web-app.forms.ipv6 :as ipv6-form]
   [web-app.event.ui]))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter

(defn- handle-upgrade-failed [tp-id]
  (rf/dispatch [:teleporter/upgrade-status {:teleporter/id tp-id :teleporter/upgrade-status "failed"}]))

(defn- on-upgrade-click [tp-id]
  ;; start upgrade timeout
  (rf/dispatch [:teleporter/upgrade-timeout tp-id (js/setTimeout #(handle-upgrade-failed tp-id) (get @config :upgrade_timeout))])

  ;; Send upgrade request to teleporter
  (rf/dispatch [:req-tp-upgrade tp-id])

  ;; We are now upgrading the teleporter
  (rf/dispatch [:teleporter/upgrading? tp-id true]))

(defn- stop-all-streams [tp-id]
  #_(send-via-mqtt! tp-id {:message/type :teleporter.cmd/hangup-all
                         :message/body {:teleporter/id tp-id}}))

(defn index [match]
  (let [id (:id (:path-params match))]
    (rf/dispatch [:req-tp-network-config id])
    (rf/dispatch [:fetch-latest-available-apt-version])

    (fn [match]
      (let [teleporters @(rf/subscribe [:teleporters])
            network-config (rf/subscribe [:teleporter/net-config id])
            apt-version-from-mqtt @(rf/subscribe [:teleporter/apt-version id])
            upgrading? (true? @(rf/subscribe [:teleporter/upgrading? id]))
            {:teleporter/keys [nickname apt-version]} (->> teleporters (filter #(= (str (:teleporter/id %)) id)) first)
            latest-reported-apt-version (or apt-version-from-mqtt apt-version)
            latest-available-apt-version @(rf/subscribe [:teleporter/latest-available-apt-version])]

        [:div.teleporter-detail-view
         [:h1 "Teleporter settings"]
         [:h2 nickname]
         [:hr]
         [:h3 "Direct commands to the Teleporter"]
         [:div.command-buttons
          [:> Button {:type "danger"
                      :on-click #(stop-all-streams id)} "Stop all streams"]]

         [:hr]
         [:h3 "Firmware version"]
         [:p (str "Current version: " latest-reported-apt-version)]
         (when (v/older? latest-reported-apt-version latest-available-apt-version)
           [:p (str "Newer version found: " latest-available-apt-version " ") [:> Button {:type "primary" :loading upgrading? :on-click #(on-upgrade-click id)} "Upgrade firmware"]])
         [:hr]
         [:h3 "Network settings"]
         [:> Tabs {:default-active-key "1"}
          [:> Tabs.TabPane {:tab "IPv4" :key "1"}
           (if (not (nil? @network-config))
             [ipv4-form/ipv4-config id]
             [:p "Fetching network configuration"])]
          #_[:> Tabs.TabPane {:tab "IPv6" :key "2"}
       [ipv6-form/ipv6-config id]]]]))))
