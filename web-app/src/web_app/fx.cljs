(ns web-app.fx
  (:require [re-frame.core :as rf]
            [ez-wire.form.helpers :refer [add-external-error
                                          remove-external-error]]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.init :refer [start-mqtt
                                  stop-mqtt]]))


(rf/reg-fx
 :rfe/push-state
 (fn [value]
   (cond (sequential? value)
         (apply rfe/push-state value)

         (keyword? value)
         (rfe/push-state value)

         :else
         (log/error :rfe/push-state value))))

(rf/reg-fx
 :form/add-external-error
 (fn [{:keys [form field-name id message valid?]}]
   (add-external-error form field-name id message valid?)))

(rf/reg-fx
 :form/remove-external-error
 (fn [{:keys [form field-name id]}]
   (remove-external-error form field-name id)))

(rf/reg-fx
 :mqtt/start
 (fn [{:auth.user/keys [channel]}]
   (stop-mqtt)
   (start-mqtt channel)))

(rf/reg-fx
 :mqtt/stop
 (fn [_]
   (stop-mqtt)))
