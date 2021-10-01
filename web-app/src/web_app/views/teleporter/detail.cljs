(ns web-app.views.teleporter.detail
  (:require
   [web-app.forms.ipv4 :as ipv4-form]
   [web-app.forms.ipv6 :as ipv6-form]
   [taoensso.timbre :as log]
   [re-frame.core :as rf]
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
     [ipv4-form/ipv4-config uuid]
     [ipv6-form/ipv6-config uuid]
     ]))
