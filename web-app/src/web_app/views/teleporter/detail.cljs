(ns web-app.views.teleporter.detail
  (:require
   [web-app.forms.ipv4 :as ipv4-form]
   [web-app.forms.ipv6 :as ipv6-form]
   [taoensso.timbre :as log]
   [re-frame.core :as rf]
   ["antd" :refer [Tabs Tabs.TabPane]]
   ))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter


(defn index [match]
  (let [uuid (:id (:path-params match))
        teleporters @(rf/subscribe [:teleporters])
        nickname (->> teleporters (filter #(= (str (:teleporter/uuid %)) uuid)) first :teleporter/nickname)]
    [:div.teleporter-detail-view
     [:h1 "Teleporter settings"]
     [:h2 nickname]
     [:h3 "Network settings:"]
     [:> Tabs {:default-active-key "1"}
      [:> Tabs.TabPane {:tab "IPv4" :key "1"}
       [ipv4-form/ipv4-config uuid]]
      [:> Tabs.TabPane {:tab "IPv6" :key "2"}
       [ipv6-form/ipv6-config uuid]]]
     ]))
