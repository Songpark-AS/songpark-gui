(ns web-app.views.room-join
  (:require ["antd" :refer [Button]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [tick.core :as t]
            [web-app.components.icon :refer [logo+slogan]]
            [web-app.forms.room :refer [roomform]]
            [web-app.views.room-host :refer [get-compareable-days
                                             get-time-display]]
            [web-app.utils :refer [clear-room!]]))

(defn invite [{{:keys [normalized-name]} :path-params}]
  (r/with-let [error (r/atom nil)
               handler (fn [data]
                         (rf/dispatch [:room.jam/knocked data]))
               error-handler (fn [{:keys [response]}]
                               (reset! error (:error/message response)))]

    ;; get rid of the query parameters as they're ugly
    (clear-room!)
    ;; when we've gotten rid of them, we run the dispatch
    (when (str/blank? js/window.location.search)
      (rf/dispatch [:room.jam/knock
                    normalized-name
                    {:handler handler
                     :error error-handler}]))
    [:div.room-invite.squeeze
     [:div.intro
      [:div.title "Knocking..."]]
     (when-let [error-msg @error]
       [:<>
        [:div.error error-msg]
        [:div.take-me-back
         {:on-click #(rfe/push-state :views/room)}
         "Take me back"]])
     [logo+slogan]]))

(defn- show-jam [days-today {:room/keys [name jammer-names last-jammed]}]
  [:div.history-entry
   [:div.left
    [:div.room-name name]
    [:div.jammers (str/join ", " jammer-names)]]
   [:div.right
    (get-time-display days-today last-jammed)]])

(defn- show-history [history]
  (let [jams @history
        days-today (get-compareable-days (t/now))]
    (when-not (empty? jams)
      [:div.history
       [:h3 "Recent"]
       (for [{:room/keys [id last-jammed] :as jam} jams]
         ^{:key [::show-history id last-jammed]}
         [show-jam days-today jam])])))

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
                                          :error error-handler}]))))
               history (rf/subscribe [:room.jam/history])]
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
        "Host a room instead"]
       [show-history history]]]]))
