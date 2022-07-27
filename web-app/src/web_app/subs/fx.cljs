(ns web-app.subs.fx
  (:require [re-frame.core :as rf]))


(rf/reg-sub
 :fx/presets
 (fn [db _]
   (:fx/presets db)))
