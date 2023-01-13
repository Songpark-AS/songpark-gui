(ns web-app.views.verify-email
  (:require ["antd" :refer [Button
                            Input]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          remove-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.signup :refer [verifyemailform]]
            [taoensso.timbre :as log]))


(defn index []
  (r/with-let [f (verifyemailform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rfe/push-state :views/login))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   :auth.user/token
                                                   :auth/verify-email
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:auth/verify-email
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.verify-email.squeeze
     [:div.intro
      [:div.title "Verify email"]]
     [:form
      {:on-submit event}
      [form/as-table {} f]
      [:> Button
       {:type "primary"
        :disabled (not (valid? @form-data))
        :on-click event}
       "Verify"]]]))
