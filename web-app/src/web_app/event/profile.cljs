(ns web-app.event.profile
  (:require [re-frame.core :as rf]
            [web-app.auth :as auth]
            [web-app.event.util :refer [add-error-message]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :profile/save
 (fn [_ [_ data handlers]]
   {:dispatch [:http/post
               (get-api-url "/profile")
               data
               handlers]}))

(rf/reg-event-db
 :profile/set
 (fn [db [_ data]]
   (assoc db :profile/profile data)))
