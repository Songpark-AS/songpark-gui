(ns web-app.views.teleporter.detail
  (:require
   ["antd" :refer [Button Tabs Tabs.TabPane]]
   [re-frame.core :as rf]
   [taoensso.timbre :as log]
   [web-app.message :refer [send-via-mqtt!]]
   [web-app.forms.ipv4 :as ipv4-form]
   #_[web-app.forms.ipv6 :as ipv6-form]
   [web-app.event.ui]))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter

(defn- stop-all-streams [tp-id]
  (send-via-mqtt! tp-id {:message/type :teleporter.cmd/hangup-all
                         :message/body {:teleporter/id tp-id}}))

(defn index [match]
  (let [uuid (:id (:path-params match))
        teleporters @(rf/subscribe [:teleporters])
        network-config (rf/subscribe [:teleporter/net-config uuid])
        {:teleporter/keys [nickname]} (->> teleporters (filter #(= (str (:teleporter/uuid %)) uuid)) first)]
    (rf/dispatch [:req-tp-network-config uuid])
    [:div.teleporter-detail-view
     [:h1 "Teleporter settings"]
     [:h2 nickname]

     [:hr]
     [:h3 "Direct commands to the Teleporter"]
     [:> Button {:type "danger"
                 :on-click #(stop-all-streams uuid)} "Stop all streams"]

     [:hr]
     [:h3 "Network settings"]
     [:> Tabs {:default-active-key "1"}
      [:> Tabs.TabPane {:tab "IPv4" :key "1"}
       (if (not (nil? @network-config))
         [ipv4-form/ipv4-config uuid]
         [:p "Fetching network configuration"])]
      #_[:> Tabs.TabPane {:tab "IPv6" :key "2"}
       [ipv6-form/ipv6-config uuid]]]]))
