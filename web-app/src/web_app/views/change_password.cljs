(ns web-app.views.change-password
  (:require ["antd" :refer [Button
                            Input]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.forgot-password :refer [changepasswordform]]))


(defn index []
  (r/with-let [f (changepasswordform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rfe/push-state :views/profile))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:auth/change-password
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.forgot-password
     [:h2 "Change password"]
     [:<>
      [:form
       {:on-submit event}
       [form/as-table {} f]]
      [:> Button
       {:disabled (not (valid? @form-data))
        :on-click event}
       "Change password"]]]))
