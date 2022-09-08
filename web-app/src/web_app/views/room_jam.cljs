(ns web-app.views.room-jam
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.icon :refer [account
                                             arrow-left-alt
                                             cancel
                                             check-circle
                                             link]]
            [taoensso.timbre :as log]))

(defn- show-jammer [room-id
                    {:keys [room/owner?]
                     user-id :auth.user/id
                     :profile/keys [name position image-url]}]
  [:div.jammer
   (if owner?
     {:class "owner"})
   [:div.image
    (if image-url
      [:img {:src image-url}]
      [account])]
   [:div.details
    [:div.name-position
     [:div.name name]
     [:div.position (if owner?
                      "You"
                      position)]]
    (if-not owner?
      [:div.actions
       [:div.remove
        {:on-click #(rf/dispatch [:room/remove room-id user-id])}
        [cancel]]])]])

(defn- show-knocker [room-id
                     {user-id :auth.user/id
                      :profile/keys [name position image-url]}]
  [:div.jammer.knocker
   [:div.image
    (if image-url
      [:img {:src image-url}]
      [account])]
   [:div.details
    [:div.name-position
     [:div.name name]
     [:div.position position]]
    [:div.actions
     [:div.decline
      {:on-click #(rf/dispatch [:room/decline room-id user-id])}
      [cancel]
      [:div.text "Decline"]]
     [:div.accept
      {:on-click #(rf/dispatch [:room/accept room-id user-id])}
      [check-circle]
      [:div.text "Accept"]]]]])


(defn- show-jammers [room-id]
  (r/with-let [owner (rf/subscribe [:room/people :owner])
               jammers (rf/subscribe [:room/people :jamming])
               knockers (rf/subscribe [:room/people :knocking])]
    [:div.jammers
     [:div.jamming
      [show-jammer room-id @owner]
      [:<>
       (for [{:keys [auth.user/id] :as jammer} @jammers]
         ^{:key [::jammer id]}
         [show-jammer room-id jammer])]]
     [:div.knockers
      (for [{:keys [auth.user/id] :as knocker} @knockers]
        ^{:key [::knocker id]}
        [show-knocker room-id knocker])]]))

(defn- show-jam [jam]
  (let [{room-id :room/id :as jammed} @jam]
    [:div.jam
     [:div.jam-action
      (if (:room/owner? jammed)
        [:div
         {:on-click #(rf/dispatch [:room.jam/close (:room/id jammed)])}
         [cancel]
         "Close room"]
        [:div
         {:on-click #(rf/dispatch [:room.jam/leave (:room/id jammed)])}
         [arrow-left-alt]
         "Leave room"])]
     [:div.room-name
      (:room/name jammed)]
     [:div.share-link
      {:on-click #(let [url (str js/window.location.href "/" (:room/name-normaliezed jammed))
                        title (str "Come join us in " (:room/name jammed))
                        text "Come jam with me"
                        data {:url url
                              :title title
                              :text text}]
                    (try
                      (js/navigator.share (clj->js data))
                      (catch js/Error e
                        (log/error "Unable to share" {:exception e
                                                      :data data}))))}
      "Share link"
      [link]]
     [show-jammers room-id]]))

(defn- show-no-jam [jam]
  (when-not @jam
   [:div.jam
    [:h2 "There is no jam in play. Leave?"]]))

(defn- show-errors [jam]
  (let [{:error/keys [key]} @jam
        msg (case key
              :room/already-hosted "You are already hosting this room"
              :room/does-not-exist "You tried to host a non-existant room"
              nil)]
    [:<>
     (when msg
       [:div.error msg])]))

(defn index []
  (r/with-let [jam (rf/subscribe [:room/jam])]
    [:<>
     [show-errors jam]
     [show-no-jam jam]
     [show-jam jam]]))
