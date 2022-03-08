(ns web-app.routes
  (:require [reitit.frontend :as r.frontend]
            [reitit.coercion.spec :as coercion.spec]
            [web-app.views.jam :as views.jam]
            [web-app.views.telemetry :as views.telemetry]
            [web-app.views.dev :as views.dev]
            [web-app.views.teleporter.list :as views.teleporter.list]
            [web-app.views.teleporter.detail :as views.teleporter.detail]))


(def routes
  [["/"
    {:name :views/dev
     :view views.dev/index}]
   ["/telemetry"
    {:name :views/telemetry
     :view views.telemetry/index}]
   ["/teleporters"
    ["" 
     {:name :views/teleporters
      :view views.teleporter.list/index}]
    ["/:id"
     {:name :views/teleporter
      :view views.teleporter.detail/index}]]])

(def router
  (r.frontend/router
   routes
   {:data {:coercion coercion.spec/coercion}}))
