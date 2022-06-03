(ns web-app.subs
  (:require [re-frame.core :as re-frame]
            [web-app.subs.auth]
            [web-app.subs.platform]
            [web-app.subs.profile]
            [web-app.subs.teleporter]
            [web-app.subs.ui]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))
