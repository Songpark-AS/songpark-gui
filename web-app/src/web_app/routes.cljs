(ns web-app.routes
  (:require [reitit.frontend :as r.frontend]
            [reitit.coercion.spec :as coercion.spec]
            [web-app.views.change-password :as views.change-password]
            [web-app.views.forgot-password :as views.forgot-password]
            [web-app.views.input :as views.input]
            [web-app.views.levels :as views.levels]
            [web-app.views.login :as views.login]
            [web-app.views.preset :as views.preset]
            [web-app.views.profile :as views.profile]
            [web-app.views.reset-password :as views.reset-password]
            [web-app.views.room :as views.room]
            [web-app.views.room-create :as views.room-create]
            [web-app.views.room-host :as views.room-host]
            [web-app.views.room-join :as views.room-join]
            [web-app.views.signup :as views.signup]
            [web-app.views.start :as views.start]
            [web-app.views.teleporter :as views.teleporter]
            [web-app.views.teleporter.pairing :as views.teleporter.pairing]
            [web-app.views.teleporter.paired :as views.teleporter.paired]
            [web-app.views.verify-email :as views.verify-email]))


(def routes
  [["/"
    {:name :views/start
     :view views.start/index}]
   ["/login"
    {:name :views/login
     :view views.login/index}]
   ["/forgot-password"
    {:name :views/forgot-password
     :view views.forgot-password/index}]
   ["/reset-password"
    {:name :views/reset-password
     :view views.reset-password/index}]
   ["/signup"
    {:name :views/signup
     :view views.signup/index}]
   ["/verify-email"
    {:name :views/verify-email
     :view views.verify-email/index}]
   ["/profile"
    {:name :views/profile
     :view views.profile/index}]
   ["/profile/change-password"
    {:name :views.profile/change-password
     :view views.change-password/index}]
   ["/preset/:input"
    {:name :views/preset
     :view views.preset/index}]
   ["/input1"
    {:name :views/input1
     :view views.input/index}]
   ["/input2"
    {:name :views/input2
     :view views.input/index}]
   ["/room"
    {:name :views/room
     :view views.room/index}]
   ["/room/host"
    {:name :views.room/host
     :view views.room-host/index}]
   ["/room/join"
    {:name :views.room/join
     :view views.room-join/index}]
   ["/room/create"
    {:name :views.room/create
     :view views.room-create/index}]
   ["/levels"
    {:name :views/levels
     :view views.levels/index}]
   ["/teleporter"
    {:name :views/teleporter
     :view views.teleporter/index}]
   ["/teleporter/pair"
    {:name :views.teleporter/pair
     :view views.teleporter.pairing/index}]
   ["/teleporter/confirm"
    {:name :views.teleporter/confirm
     :view views.teleporter.pairing/confirm-link}]
   ["/teleporter/paired"
    {:name :views.teleporter/paired
     :view views.teleporter.paired/index}]])

(def router
  (r.frontend/router
   routes
   {:data {:coercion coercion.spec/coercion}}))
