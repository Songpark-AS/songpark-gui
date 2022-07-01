(ns web-app.subs.room
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :room/room
 (fn [db _]
   (:room/room db)))

(rf/reg-sub
 :room/jammers
 (fn [db _]
   (let [jammers (get db :room/jammers [{:profile/name "Christian Ruud"
                                         :auth.user/id 1
                                         :profile/image-url "http://localhost:3000/static/images/christian.jpg"
                                         :profile/position "Guitar"
                                         :jammer/muted? true
                                         :jammer/volume 30}])]
     (map :auth.user/id jammers))))

(rf/reg-sub
 :room/jammer
 (fn [db [_ auth-id]]
   (get-in db [:room/jammers auth-id] {:profile/name "Christian Ruud"
                                       :profile/image-url "http://localhost:3000/static/images/christian.jpg"
                                       :profile/position "Guitar"
                                       :jammer/muted? true
                                       :jammer/volume 30})))
