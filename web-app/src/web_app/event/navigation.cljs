(ns web-app.event.navigation
  (:require [re-frame.core :as rf]
            [web-app.utils :refer [rooms]]))


(rf/reg-event-db
 :navigation/route
 (fn [db [_ route]]
   (let [navigation (get db :navigation/routes (list))
         navigation-room (get db :navigation.room/last-known :views/room)]
     (as-> db $
       (assoc $ :navigation/routes (conj (take 1 navigation) route))
       (if (rooms route)
         (assoc $ :navigation.room/last-known route)
         $)))))
