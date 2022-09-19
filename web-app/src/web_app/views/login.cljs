(ns web-app.views.login
  (:require ["antd" :refer [Button
                            Input
                            Input.Password]]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.login :refer [loginform]]
            [web-app.utils :refer [clear-room!]]))


(defn index [_]
  (r/with-let [f (loginform {:label? false}
                            {:auth.user/email nil
                             :auth.user/password nil})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rf/dispatch [:auth/user data])
                         (rf/dispatch [:app/init])
                         (rfe/push-state :views/room))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:auth/login
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    (clear-room!)
    [:div.login.squeeze
     [:div.intro
      [:div.title "Welcome back"]
      [:div.slogan "Log in to your account"]]
     [:form
      {:on-submit event}
      [form/as-table {} f]
      [:div.forgot-password
       {:on-click #(rfe/push-state :views/forgot-password)}
       "Forgot password?"]
      [:> Button
       {:type "primary"
        :disabled (not (valid? @form-data))
        :on-click event}
       "Login"]]

     [:div.signup
      {:on-click #(rfe/push-state :views/signup)}
      "Don't have an account? Sign up."]]))
