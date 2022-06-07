(ns web-app.views.room-create
  (:require ["antd" :refer [Button
                            Input
                            Input.Password]]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [web-app.forms.room :refer [roomform]]))

(defn index []
  (r/with-let [f (roomform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rf/dispatch [:app.init/room data])
                         (rfe/push-state :views.room/host))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:room/create
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.room-create
     [:h2 "New room"]
     [:div "Choose a name for you room."]
     [:form
      {:on-submit event}
      [form/as-table {} f]
      [:> Button
       {:type "primary"
        :disabled (not (valid? @form-data))
        :on-click event}
       "Create"]]]))
