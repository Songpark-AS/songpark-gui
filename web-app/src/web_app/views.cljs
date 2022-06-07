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

(defn main []
  (r/with-let [user (rf/subscribe [:auth/whoami])]
    (let [matched @match
          data (:data matched)
          current-view (:name data)]
      (if (and (auth/logged-out? @user)
               (not (#{:views/login
                       :views/signup
                       :views/forgot-password
                       :views/reset-password
                       :views/verify-email} current-view)))
        (do (rfe/push-state :views/login)
            "")
        [:> Layout
         [:> Layout.Content
          [:<>
           [views.topbar/index]
           [:div.content-wrapper
            (if matched
              (let [view (:view data)]
                [view matched]))]
           [views.footer/index]]]]))))

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [main]]])
