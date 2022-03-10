(ns web-app.subs.ui
  (:require [re-frame.core :as rf]
            [web-app.subs.util :refer [get-selected-teleporter]]))



(rf/reg-sub
 :teleporter.view/selected-teleporter
 (fn [db _]
   (get-selected-teleporter db)))

(rf/reg-sub
 :component/radio-group
 (fn [db [_ group-key default-value]]
   (get-in db [:component/radio-group group-key] default-value)))



(comment
  @(rf/subscribe [:data/msg])
  @(rf/subscribe [:teleporters])
  @(rf/subscribe [:teleporter/net-config #uuid "ad6fc5b7-c52c-5941-bfb7-cf4fb4189775"])

  

  )
