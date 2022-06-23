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
            [web-app.forms.login :refer [loginform]]))


(defn index [_]
  (r/with-let [f (loginform {:label? false}
                            {:auth.user/email nil
                             :auth.user/password nil})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rf/dispatch [:auth/user data])
                         (rf/dispatch [:app/init])
                         (rf/dispatch [:mqtt/subscribe (:auth.user/channel data)])
                         (rfe/push-state :views/home))
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
    [:<>
     [:h2 "Login to Songpark Live"]
     [:form
      {:on-submit event}
      [form/as-table {} f]
      [:> Button
       {:type "primary"
        :disabled (not (valid? @form-data))
        :on-click event}
       "Login"]]
     [:div.signup
      [:> Button
       {:on-click #(rfe/push-state :views/forgot-password)}
       "Forgot password?"]]
     [:div.signup
      [:> Button
       {:on-click #(rfe/push-state :views/signup)}
       "Signup"]]]))
