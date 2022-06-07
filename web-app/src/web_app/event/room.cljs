(ns web-app.event.room
  (:require [re-frame.core :as rf]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(rf/reg-event-fx
 :room/create
 (fn [_ [_ data handlers]]
   {:dispatch [:http/put
               (get-api-url "/room")
               data
               handlers]}))
