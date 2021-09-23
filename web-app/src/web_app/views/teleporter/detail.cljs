(ns web-app.views.teleporter.detail
  (:require
   [web-app.forms.ipv4 :as ipv4-form]
   ))

;; Here be detailview of a teleporter
;; This view will contain configuration options for a teleporter


(defn index []
  [:div
   [:h1 "Teleporter settings"]
   [ipv4-form/ipv4-config "foo"]
   ])
