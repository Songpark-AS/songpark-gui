(ns web-app.event.auth
  (:require [re-frame.core :as rf]
            [web-app.auth :as auth]
            [web-app.event.util :refer [add-error-message]]
            [web-app.utils :refer [get-api-url get-platform-url]]))

(rf/reg-event-fx
 :auth/whoami
 (fn [_ _]
   {:dispatch [:http/get
               (get-platform-url "/auth")
               nil
               :auth.whoami/success
               :auth.whoami/error]}))

(rf/reg-event-fx
 :auth.whoami/success
 (fn [{:keys [db]} [_ data]]
   (if (auth/logged-in? data)
     {:db (assoc db :auth/user data)
      :fx [[:dispatch [:app/init]]
           [:dispatch [:mqtt/subscribe (:auth.user/channel data)]]]}
     {:db db})))

(rf/reg-event-db
 :auth.whoami/error
 (fn [db [_ {:keys [response]}]]
   (add-error-message (assoc db
                             :auth/user nil)
                      response)))

(rf/reg-event-fx
 :auth/forgot-password
 (fn [_ [_ email]]
   {:dispatch [:http/post
               (get-platform-url "/auth/forgotten-password")
               {:auth.user/email email}]}))

(rf/reg-event-fx
 :auth/reset-password
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-platform-url "/auth/reset-password")
               data
               handlers]}))

(rf/reg-event-fx
 :auth/change-password
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-platform-url "/auth/change-password")
               data
               handlers]}))

(rf/reg-event-fx
 :auth/login
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-platform-url "/auth/login")
               data
               handlers]}))

(rf/reg-event-db
 :auth/user
 (fn [db [_ user]]
   (assoc db :auth/user user)))

(rf/reg-event-fx
 :auth/signup
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-platform-url "/auth/signup")
               data
               handlers]}))

(rf/reg-event-fx
 :auth/verify-email
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-platform-url "/auth/verify-email")
               data
               handlers]}))

(rf/reg-event-fx
 :auth/logout
 (fn [_ _]
   {:dispatch [:http/post
               (get-platform-url "/auth/logout")
               nil
               :auth.logout/success]}))

(rf/reg-event-db
 :auth.logout/success
 (fn [db _]
   ;; empty the database
   {:db {}
    :dispatch [:mqtt/unsubscribe]}))
