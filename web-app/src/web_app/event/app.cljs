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
                     :app.init/profile]]
         [:dispatch [:http/get
                     (get-api-url "/room")
                     {}
                     :app.init/rooms]]]}))


(rf/reg-event-db
 :app.init.profile/pronouns
 (fn [db [_ data]]
   (assoc db :profile/pronouns data)))

(rf/reg-event-db
 :app.init/profile
 (fn [db [_ data]]
   (assoc db :profile/profile data)))

(rf/reg-event-db
 :app.init/rooms
 (fn [db [_ data]]
   (assoc db :room/room data)))

(rf/reg-event-db
 :app.init/room
 (fn [db [_ data]]
   (update db :room/room conj data)))
