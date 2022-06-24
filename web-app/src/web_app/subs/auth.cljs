(ns web-app.subs.auth
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :auth/whoami
 (fn [db _]
   (:auth/user db)))
