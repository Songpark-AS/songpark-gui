(ns web-app.views.signup
  (:require ["antd" :refer [Button
                            Input
                            Input.Password]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.signup :refer [signupform]]))


(defn index []
  (r/with-let [f (signupform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rf/dispatch [:auth/user data])
                         (rfe/push-state :views/verify-email))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:auth/signup
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:<>
     [:div.signup.squeeze
      [:div.intro
       [:div.title "Sign up"]
       [:div.slogan "Create your account"]]
      [:form
       {:on-submit event}
       [form/as-table {} f]
       [:div.login
        {:on-click #(rfe/push-state :views/login)}
        "Go back to the login screen"]
       [:> Button
        {:type "primary"
         :disabled (not (valid? @form-data))
         :on-click event}
        "Create"]]]]))
