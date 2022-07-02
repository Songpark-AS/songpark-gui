(ns web-app.subs.room
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :room/room
 (fn [db _]
   (:room/room db)))

(rf/reg-sub
 :room/jammers
 (fn [db _]
   (->> (get db :room/jammers)
        (keys))))

(comment
  ;; for dev purposes
  (swap! re-frame.db/app-db assoc :room/jammers {1 {:profile/name "Christian Ruud"
                                                    :auth.user/id 1
                                                    :profile/image-url "http://localhost:3000/static/images/christian.jpg"
                                                    :profile/position "Guitar"
                                                    :jammer/muted? true
                                                    :jammer/volume 30}})
  (get @re-frame.db/app-db :room/jammers)
  )

(rf/reg-sub
 :room/jammer
 (fn [db [_ auth-id]]
   (get-in db [:room/jammers auth-id])))
