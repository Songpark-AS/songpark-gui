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
         user-id (-> db :auth/user :auth.user/id)
         others
         (->> (get-in db [:room/jam :room/jammers])
              (vals)
              (mapv (fn [{:keys [auth.user/id]
                          tp-id :teleporter/id
                          :as jammer}]
                      (assoc jammer
                             :room/owner? (= owner-id id)
                             :jammer/you? (= user-id id)))))]
     (case what
       :owner (->> others
                   (filter :room/owner?)
                   first)
       :jamming (->> others
                     (filter #(= :jamming (:jammer/status %)))
                     (remove :room/owner?))
       :other-jammers (->> others
                           (filter #(= :jamming (:jammer/status %)))
                           (remove :jammer/you?))
       :knocking (->> others
                      (filter #(= :knocking (:jammer/status %)))
                      (remove :room/owner?))
       nil))))

(rf/reg-sub
 :room/jammer
 (fn [db [_ auth-id]]
   (get-in db [:room/jam :room/jammers auth-id])))

(rf/reg-sub
 :room/jam
 (fn [db _]
   (let [{:keys [room/jam auth/user]} db]
     (-> jam
         (assoc :room/owner? (= (:room/owner jam) (:auth.user/id user)))
         (dissoc :room/jammers)))))

(rf/reg-sub
 :room/jamming?
 (fn [db _]
   (get-in db [:room/jam :room/id])))

(rf/reg-sub
 :room.jam/history
 (fn [db _]
   (:room.jam/history db)))
