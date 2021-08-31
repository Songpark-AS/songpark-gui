(ns example.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-counter
 (fn [db _]
   (:counter db)))
(reg-sub
 :view
 (fn [db _]
   (:view db)))

(reg-sub
 :balance
 (fn [db _]
   (get-in db [:studio :balance] 0)
   )
 )
(reg-sub
 :balance-slider
 (fn [db _]
   (get-in db [:studio :balance-slider] 0))
 )
