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
    [:div.forgot-password
     [:h2 "Reset your password"]
     (if @success?
       [:div.info
        "You have successfully reset your password"
        [:> Button
         {:on-click #(rfe/push-state :views/home)}
         "Take me back"]]
       [:<>
        [:div.info
         "An email has been sent to you with the token required to reset your password."]
        [:form
         [form/as-table {} f]
         [:> Button
          {:disabled (not (valid? @form-data))
           :on-click event}
          "Reset my password"]]
        #_[:> Button
           {:type "primary"
            :on-click #(rfe/push-state :views/login)}
           "Take me back to the login view"]])]))
