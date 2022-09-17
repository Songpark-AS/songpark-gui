(ns web-app.event.auth
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
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

(defn- get-room [text]
  (when-not (and (str/blank? text)
                 (str/starts-with? text "?")))
  (subs text 1))

(rf/reg-event-fx
 :auth.whoami/success
 (fn [{:keys [db]} [_ data]]
   (if (auth/logged-in? data)
     (merge {:db (assoc db :auth/user data)}
            (if-let [room (get-room js/window.location.search)]
              {:fx [[:dispatch [:app/init]]
                    [:rfe/push-state [:views.room.join/invite {:normalized-name room}]]]}
              {:fx [[:dispatch [:app/init]]
                    [:rfe/push-state :views/room]]}))
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

(rf/reg-event-fx
 :auth.logout/success
 (fn [{:keys [db]} _]
   ;; empty the database
   {:db {:app/initialized? true}
    :fx [[:mqtt/stop true]
         [:rfe/push-state :views/start]]}))
