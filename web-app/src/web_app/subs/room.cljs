(ns web-app.subs.room
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :room/room
 (fn [db _]
   (:room/room db)))

(rf/reg-sub
 :room/people
 (fn [db [_ what]]
   (let [owner-id (-> db :room/jam :room/owner)
         [owner & others]
         (->> (get-in db [:room/jam :room/jammers])
              (vals)
              (mapv (fn [jammer]
                      (assoc jammer :room/owner? (= owner-id (:auth.user/id jammer))))))]
     (case what
       :owner owner
       :jamming (->> others
                     (filter #(= :jamming (:jammer/status %))))
       :knocking (->> others
                      (filter #(= :knocking (:jammer/status %))))
       nil))))

(rf/reg-sub
 :room/jammer
 (fn [db [_ auth-id]]
   (get-in db [:room/jam :room/jammers auth-id])))

(comment

  )

(rf/reg-sub
 :room/jam
 (fn [db _]
   (let [{:keys [room/jam auth/user]} db]
     (-> jam
         (assoc :room/owner? (= (:room/owner jam) (:auth.user/id user)))
         (dissoc :room/jammers)))))
