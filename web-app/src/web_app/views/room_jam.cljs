(ns web-app.views.room-jam
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.components.filler :refer [filler]]
            [web-app.components.icon :refer [account
                                             arrow-left-alt
                                             cancel
                                             check-circle
                                             link]]
            [taoensso.timbre :as log]))

(defn- show-jammer [room-id
                    {:keys [room/owner?
                            jammer/you?]
                     user-id :auth.user/id
                     :profile/keys [name position image-url]}
                    i-own-the-room?]
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
     [:div.position (if you?
                      "You"
                      position)]]
    (if (and i-own-the-room?
             (not owner?))
      [:div.actions
       [:div.remove
        {:on-click #(rf/dispatch [:room.jam/remove room-id user-id])}
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
      {:on-click #(rf/dispatch [:room.jam/decline room-id user-id])}
      [cancel]
      [:div.text "Decline"]]
     [:div.accept
      {:on-click #(rf/dispatch [:room.jam/accept room-id user-id])}
      [check-circle]
      [:div.text "Accept"]]]]])

(defn- status-indicator [jam-status jammers]
  (let [{:jam/keys [sip stream sync status]} @jam-status]
    [:div.status
     {:class (and
              (> (count @jammers) 0)
              (cond
                (or (#{:stream/broken :stream/stopped} stream)
                    (#{:sync/sync-failed} sync)) "failed"

                (#{:sip/call-ended} sip) "idle"

                (or (#{:sip/in-call :sip/call :sip/calling} sip)
                    (#{:stream/streaming} stream)
                    (#{:sync/syncing :sync/synced} sync)) "streaming"

                :else "idle"))}]))

(defn- show-jammers [room-id owner? status]
  (r/with-let [owner (rf/subscribe [:room/people :owner])
               jammers (rf/subscribe [:room/people :jamming])
               knockers (rf/subscribe [:room/people :knocking])]
    [:div.jammers
     [status-indicator status jammers]
     [:div.jamming
      [show-jammer room-id @owner]
      [:<>
       (for [{:keys [auth.user/id] :as jammer} @jammers]
         ^{:key [::jammer id]}
         [show-jammer room-id jammer owner?])]]
     [:div.knockers
      (for [{:keys [auth.user/id] :as knocker} @knockers]
        ^{:key [::knocker id]}
        [show-knocker room-id knocker])]
     [filler]]))

(defn- show-jam [jam status]
  (let [{room-id :room/id
         :room/keys [knocking? owner?]
         :as jammed} @jam]
    (when-not knocking?
      [:div.jam
       [:div.jam-action
        (if (:room/owner? jammed)
          [:div
           {:on-click #(rf/dispatch [:room.jam/close room-id])}
           [cancel]
           "Close room"]
          [:div
           {:on-click #(rf/dispatch [:room.jam/leave room-id])}
           [arrow-left-alt]
           "Leave room"])]
       [:div.room-name
        (:room/name jammed)]
       ;; [:div.share-link
       ;;  {:on-click #(let [url (str js/window.location.href "/" (:room/name-normaliezed jammed))
       ;;                    title (str "Come join us in " (:room/name jammed))
       ;;                    text "Come jam with me"
       ;;                    data {:url url
       ;;                          :title title
       ;;                          :text text}]
       ;;                (try
       ;;                  (js/navigator.share (clj->js data))
       ;;                  (catch js/Error e
       ;;                    (log/error "Unable to share" {:exception e
       ;;                                                  :data data}))))}
       ;;  "Share link"
       ;;  [link]]
       [show-jammers room-id owner? status]])))

(defn- show-no-jam [jam]
  (when-not @jam
   [:div.jam
    [:h2 "There is no jam in play. Leave?"]]))

(defn show-knocking [jam]
  (when (:room/knocking? @jam)
    [:div.jam
     (let [{room-id :room/id
            :keys [room/knocking?]
            :as jammed} @jam]
       [:div.knocking
        [:div.jam-action>div
         {:on-click #(rf/dispatch [:room.jam/leave room-id])}
         [arrow-left-alt]
         "Leave room"]
        [:div.room-name
         (:room/name jammed)]
        [:div.punchline "Knock, knock!"]])]))

(defn- show-errors [jam]
  (let [{:error/keys [key]} @jam
        msg (case key
              :room/already-hosted "You are already hosting this room"
              :room/does-not-exist "The room no longer exist"
              nil)]
    [:<>
     (when msg
       [:div.error msg])]))

(defn index []
  (r/with-let [jam (rf/subscribe [:room/jam])
               status (rf/subscribe [:teleporter/jam-status])]
    [:<>
     [show-errors jam]
     [show-no-jam jam]
     [show-jam jam status]
     [show-knocking jam]]))
