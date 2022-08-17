(ns web-app.views.forgot-password
  (:require ["antd" :refer [Button
                            Input]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.forgot-password :refer [fwform]]))

(defn index []
  (r/with-let [f (fwform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])]
    [:div.forgot-password.squeeze
     [:div.intro
      [:div.title "Forgot your password?"]]
     [:<>
      [:form
       [form/as-table {} f]
       [:div.login
        {:on-click #(rfe/push-state :views/login)}
        "Go back to the login screen"]
       [:> Button
        {:disabled (not (valid? @form-data))
         :on-click #(do (rf/dispatch [:auth/forgot-password
                                      (:auth.user/email @form-data)])
                        (rfe/push-state :views/reset-password))}
        "Send instructions"]]]]))
