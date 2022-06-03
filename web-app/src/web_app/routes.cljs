(ns web-app.routes
  (:require [reitit.frontend :as r.frontend]
            [reitit.coercion.spec :as coercion.spec]
            [web-app.views.login :as views.login]
            [web-app.views.forgot-password :as views.forgot-password]
            [web-app.views.home :as views.home]
            [web-app.views.profile :as views.profile]
            [web-app.views.reset-password :as views.reset-password]
            [web-app.views.signup :as views.signup]
            [web-app.views.verify-email :as views.verify-email]))


(def routes
  [["/"
    {:name :views/home
     :view views.home/index}]
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
     :view views.profile/index}]])

(def router
  (r.frontend/router
   routes
   {:data {:coercion coercion.spec/coercion}}))
