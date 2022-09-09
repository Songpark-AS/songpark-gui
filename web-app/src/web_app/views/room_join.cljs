(ns web-app.views.room-join
  (:require ["antd" :refer [Button]]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.forms.room :refer [roomform]]))

(defn index []
  (r/with-let [f (roomform {:label? false} {})
               form-data (rf/subscribe [::form/on-valid (:id f)])
               handler (fn [data]
                         (rf/dispatch [:room.jam/knocked data]))
               error-handler (fn [{:keys [response]}]
                               (log/debug response)
                               (add-external-error f
                                                   :room/name
                                                   :room/name
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data]
                         (when (valid? data)
                           (rf/dispatch [:room.jam/knock
                                         (:room/name data)
                                         {:handler handler
                                          :error error-handler}]))))]
    [:div.room-join
     [:div.intro
      [:div.title "Join room"]
      [:div.slogan "Ask the owner for the room name"]]
     [:form
      {:on-submit event}
      [form/as-table {} f]
      [:div.container
       [:> Button
        {:type "primary"
         :disabled (not (valid? @form-data))
         :on-click event}
        "Join"]
       [:div.host-room
        {:on-click #(rfe/push-state :views.room/host)}
        "Host a room instead"]]]]))
