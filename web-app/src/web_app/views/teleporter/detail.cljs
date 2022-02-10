(ns web-app.views.teleporter.detail
  (:require
   ["antd" :refer [Button Tabs Tabs.TabPane]]
   [re-frame.core :as rf]
   [taoensso.timbre :as log]
   [web-app.message :refer [send-via-mqtt!]]
   [web-app.forms.ipv4 :as ipv4-form]
   [version-clj.core :as v]
   #_[web-app.forms.ipv6 :as ipv6-form]
   [web-app.event.ui]))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter

(defn- on-upgrade-click [tp-id]
  (rf/dispatch [:req-tp-upgrade tp-id])
  (rf/dispatch [:teleporter/upgrading? tp-id true]))

(defn- stop-all-streams [tp-id]
  (send-via-mqtt! tp-id {:message/type :teleporter.cmd/hangup-all
                         :message/body {:teleporter/id tp-id}}))

(defn index [match]
  (let [uuid (:id (:path-params match))
        teleporters @(rf/subscribe [:teleporters])
        network-config (rf/subscribe [:teleporter/net-config uuid])
        apt-version-from-mqtt (rf/subscribe [:teleporter/apt-version uuid])
        upgrading? (rf/subscribe [:teleporter/upgrading? uuid])
        upgrade-status (rf/subscribe [:teleporter/upgrade-status uuid])
        upgrade-complete? (= "complete" @upgrade-status)
        {:teleporter/keys [nickname apt-version]} (->> teleporters (filter #(= (str (:teleporter/uuid %)) uuid)) first)
        latest-reported-apt-version (or @apt-version-from-mqtt apt-version)
        latest-available-apt-version @(rf/subscribe [:teleporter/latest-available-apt-version])]
    (rf/dispatch [:req-tp-network-config uuid])
    (rf/dispatch [:fetch-latest-available-apt-version])
    [:div.teleporter-detail-view
     [:h1 "Teleporter settings"]
     [:h2 nickname]

     [:hr]
     [:h3 "Direct commands to the Teleporter"]
     [:div.command-buttons
      [:> Button {:type "danger"
                  :on-click #(stop-all-streams uuid)} "Stop all streams"]]

     [:hr]
     [:h3 "Firmware version"]
     [:p (str "Current version: " latest-reported-apt-version)]
     (when (v/older? latest-reported-apt-version latest-available-apt-version)
       [:p (str "Newer version found: " latest-available-apt-version " ") [:> Button {:type "primary" :loading (if (and @upgrading? (not upgrade-complete?)) true false) :on-click #(on-upgrade-click uuid)} "Upgrade firmware"]])
     [:hr]
     [:h3 "Network settings"]
     [:> Tabs {:default-active-key "1"}
      [:> Tabs.TabPane {:tab "IPv4" :key "1"}
       (if (not (nil? @network-config))
         [ipv4-form/ipv4-config uuid]
         [:p "Fetching network configuration"])]
      #_[:> Tabs.TabPane {:tab "IPv6" :key "2"}
       [ipv6-form/ipv6-config uuid]]]]))
