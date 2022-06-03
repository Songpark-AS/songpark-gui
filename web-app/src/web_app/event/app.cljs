(ns web-app.event.app
  (:require [re-frame.core :as rf]
            [web-app.event.util :refer [add-error-message]]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :app/init
 (fn [_ _]
   {:fx [[:dispatch [:http/get
                     (get-api-url "/profile/pronouns")
                     {}
                     :app.init.profile/pronouns]]
         [:dispatch [:http/get
                     (get-api-url "/profile")
                     {}
                     :app.init/profile]]]}))


(rf/reg-event-db
 :app.init.profile/pronouns
 (fn [db [_ data]]
   (assoc db :profile/pronouns data)))

(rf/reg-event-db
 :app.init/profile
 (fn [db [_ data]]
   (assoc db :profile/profile data)))
