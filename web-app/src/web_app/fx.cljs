(ns web-app.fx
  (:require [re-frame.core :as rf]
            [ez-wire.form.helpers :refer [add-external-error
                                          remove-external-error]]
            [reitit.frontend.easy :as rfe]))


(rf/reg-fx
 :rfe/push-state
 (fn [value]
   (apply rfe/push-state value)))

(rf/reg-fx
 :form/add-external-error
 (fn [{:keys [form field-name id message valid?]}]
   (add-external-error form field-name id message valid?)))

(rf/reg-fx
 :form/remove-external-error
 (fn [{:keys [form field-name id]}]
   (remove-external-error form field-name id)))
