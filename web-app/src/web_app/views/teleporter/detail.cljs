(ns web-app.views.teleporter.detail
  (:require
   ["antd" :refer [Button Tabs Tabs.TabPane]]
   [re-frame.core :as rf]
   [taoensso.timbre :as log]
   [web-app.message :refer [send-via-mqtt!]]
   [web-app.forms.ipv4 :as ipv4-form]
   [web-app.forms.ipv6 :as ipv6-form]))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter

(defn- stop-all-streams [tp-id]
  (send-via-mqtt! tp-id {:message/type :teleporter.cmd/hangup-all
                         :message/body {:teleporter/id tp-id}}))

(defn- path-reset [tp-id]
  (send-via-mqtt! tp-id {:message/type :teleporter.cmd/path-reset
                         :message/body {:teleporter/id tp-id}}))

(defn index [match]
  (let [uuid (:id (:path-params match))
        teleporters @(rf/subscribe [:teleporters])
        {:teleporter/keys [nickname]} (->> teleporters (filter #(= (str (:teleporter/uuid %)) uuid)) first)]
    [:div.teleporter-detail-view
     [:h1 "Teleporter settings"]
     [:h2 nickname]

     [:hr]
     [:h3 "Direct commands to the Teleporter"]
     [:div.command-buttons
      [:> Button {:type "danger"
                  :on-click #(stop-all-streams uuid)} "Stop all streams"]
      [:> Button {:type "danger"
                  :on-click #(path-reset uuid)} "Path reset"]]

     [:hr]
     [:h3 "Network settings"]
     [:> Tabs {:default-active-key "1"}
      [:> Tabs.TabPane {:tab "IPv4" :key "1"}
       [ipv4-form/ipv4-config uuid]]
      [:> Tabs.TabPane {:tab "IPv6" :key "2"}
       [ipv6-form/ipv6-config uuid]]]]))
