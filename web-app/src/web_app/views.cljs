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
     :views/verify-email
     :views/start} current-view))

(defn- teleporter-views? [current-view]
  (#{:views/room
     :views.room/host
     :views.room/jam
     :views.room/create
     :views.room/join
     :views/levels
     :views/input1
     :views/input2}
   current-view))

(defn splash-screen []
  [:div.splash
   [:div.container
    [:img {:width "350px"
           :src "/img/logo.svg"}]
    [:div.intro
     [:div.title "songpark"]
     [:div.slogan "never play alone"]]]])

(defn main []
  (r/with-let [initialized? (rf/subscribe [:app/initialized?])
               user (rf/subscribe [:auth/whoami])]
    (let [matched @match
          data (:data matched)
          current-view (:name data)
          login-view? (login-views? current-view)
          teleporter-view? (teleporter-views? current-view)]
      ;; (log/debug {:current-view current-view
      ;;             :user @user
      ;;             :logged-out? (auth/logged-out? @user)
      ;;             :login-view? login-view?
      ;;             :!login-view (not login-view?)})
      (rf/dispatch [:navigation/route current-view])
      (if (and (auth/logged-out? @user)
               (not login-view?))
        (do (rfe/push-state :views/start)
            [splash-screen])
        [:> Layout
         [:> Layout.Content
          [:<>
           (when teleporter-view?
             [views.topbar/index])
           [:div#content-wrapper.content-wrapper
            (if matched
              (let [view (:view data)]
                [view matched]))]
           (when teleporter-view?
             [views.footer/index current-view])]]]))))

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [main]]])
