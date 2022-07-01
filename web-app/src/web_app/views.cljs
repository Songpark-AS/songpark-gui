(ns web-app.views
  (:require ["antd" :refer [Layout Layout.Content]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.auth :as auth]
            [web-app.views.footer :as views.footer]
            [web-app.views.login :as views.login]
            [web-app.views.topbar :as views.topbar]))

(defonce match (r/atom nil))

(defn- login-views? [current-view]
  (#{:views/login
     :views/signup
     :views/forgot-password
     :views/reset-password
     :views/verify-email} current-view))

(defn splash-screen []
  [:div "Splash screen"])

(defn main []
  (r/with-let [initialized? (rf/subscribe [:app/initialized?])
               user (rf/subscribe [:auth/whoami])]
    (let [matched @match
          data (:data matched)
          current-view (:name data)
          login-view? (login-views? current-view)]
      ;; (log/debug {:current-view current-view
      ;;             :user @user
      ;;             :logged-out? (auth/logged-out? @user)
      ;;             :login-view? login-view?
      ;;             :!login-view (not login-view?)})
      (if (and (auth/logged-out? @user)
               (not login-view?))
        (do (rfe/push-state :views/login)
            "")
        [:> Layout
         [:> Layout.Content
          [:<>
           (when-not login-view?
             [views.topbar/index])
           [:div.content-wrapper
            (if matched
              (let [view (:view data)]
                [view matched]))]
           (when-not login-view?
             [views.footer/index current-view])]]]))))

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [main]]])
