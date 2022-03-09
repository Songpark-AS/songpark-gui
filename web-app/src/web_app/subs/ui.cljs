(ns web-app.subs.ui
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :teleporter.view/selected-teleporter
 (fn [db _]
   (let [tp-id (or (get db :teleporter.view/selected-teleporter)
                   (->> db
                        :teleporters
                        (vals)
                        (sort-by :teleporter/nickname)
                        first
                        :teleporter/id))]
     (get-in db [:teleporters tp-id]))))

(rf/reg-sub
 :tp-list-selection-mode
 (fn [db _]
   (:tp-list-selection-mode db)))

(rf/reg-sub
 :selected-teleporters
 (fn [db _]
   (:selected-teleporters db)))

(rf/reg-sub
 :selected-teleporters-staging
 (fn [db _]
   (:selected-teleporters-staging db)))


(comment
  @(rf/subscribe [:data/msg])
  @(rf/subscribe [:teleporters])
  @(rf/subscribe [:teleporter/net-config #uuid "ad6fc5b7-c52c-5941-bfb7-cf4fb4189775"])

  

  )
