(ns web-app.subs.profile
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :profile/profile
 (fn [db _]
   (:profile/profile db)))

(rf/reg-sub
 :profile/pronouns
 (fn [db _]
   (:profile/pronouns db)))
