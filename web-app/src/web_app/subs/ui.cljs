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


(rf/reg-sub
 :ui.fx.input/tab
 (fn [db [_ input default]]
   (get-in db [:ui.fx.input/tab input] (str input default))))
