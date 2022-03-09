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
 :component/radio-group
 (fn [db [_ group-key default-value]]
   (get-in db [:component/radio-group group-key] default-value)))



(comment
  @(rf/subscribe [:data/msg])
  @(rf/subscribe [:teleporters])
  @(rf/subscribe [:teleporter/net-config #uuid "ad6fc5b7-c52c-5941-bfb7-cf4fb4189775"])

  

  )
