(ns web-app.routes
  (:require [reitit.frontend :as r.frontend]
            [reitit.coercion.spec :as coercion.spec]
            [web-app.views.jam :as views.jam]
            [web-app.views.logs :as views.logs]
            [web-app.views.teleporter.list :as views.teleporter.list]
            [web-app.views.teleporter.detail :as views.teleporter.detail]))


(def routes
  [["/"
    {:name :views/jam
     :view views.jam/index}]
   ["/logs"
    {:name :views/logs
     :view views.logs/index}]
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
