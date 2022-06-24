(ns web-app.subs.app
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :app/initialized?
 (fn [db _]
   (:app/initialized? db)))
