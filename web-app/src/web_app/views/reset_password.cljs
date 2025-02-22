(ns web-app.views.reset-password
  (:require ["antd" :refer [Button
                            Input]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [valid?
                                          add-external-error]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.forgot-password :refer [passwordform]]))

(defn index []
  (r/with-let [f (passwordform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               success? (r/atom false)
               handler (fn [data]
                         (reset! success? true))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:auth/reset-password
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.reset-password.squeeze
     [:div.intro
      [:div.title "Reset your password"]
      [:div.slogan
       (if @success?
         "You have successfully reset your password"
         "An email has been sent to you with the token required to reset your password.")]]
     (if @success?
       [:div.info
        [:> Button
         {:type "primary"
          :on-click #(rfe/push-state :views/room)}
         "Take me back"]]
       [:<>
        [:div.info
         {:on-click #(rfe/push-state :views/login)}
         "Take me back to the login view"]

        [:form
         [form/as-table {} f]
         [:> Button
          {:type "primary"
           :disabled (not (valid? @form-data))
           :on-click event}
          "Reset my password"]]])]))
