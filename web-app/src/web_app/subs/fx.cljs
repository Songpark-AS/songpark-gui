(ns web-app.subs.fx
  (:require [re-frame.core :as rf]
            [web-app.subs.util :refer [get-tp-id]]))


(rf/reg-sub
 :fx/presets
 (fn [db _]
   (:fx/presets db)))


(rf/reg-sub
 :fx.preset/current
 (fn [db _]
   (let [tp-id (get-tp-id db nil)
         preset-id (get-in db [:teleporters tp-id :fx.preset/current])]
     (reduce (fn [_ fx]
               (if (= (:fx.preset/id fx) preset-id)
                 (reduced fx)
                 nil))
             nil (:fx/presets db)))))
