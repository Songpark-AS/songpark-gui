(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                          find-doc print-doc pst source)]
            [clojure.pprint :refer (pprint)]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))



(comment

  (get-in @app-db [:auth/user])
  (get-in @app-db [:room/jam :room/jammers])
  (get-in @app-db [:room/room])
  @(rf/subscribe [:room/jam])
  @(rf/subscribe [:room/people :knocking])

  (rf/dispatch [:app/init])
  (rf/dispatch [:room.jam/host 2])

  ;; for dev purposes
  (swap! app-db assoc-in [:room/jam :room/jammers]
         {1 {:profile/name "Emil Bengtsson"
             :auth.user/id 1
             :profile/position "Vocals (deep bass)"}
          5 {:profile/name "Test McTest"
             :auth.user/id 5
             :jammer/status :jamming
             :profile/position "The world's smallest violin"}
          6 {:profile/name "Christian Ruud"
             :auth.user/id 6
             :profile/image-url "http://localhost:3000/static/images/christian.jpg"
             :profile/position "Guitar"
             :jammer/status :knocking
             :jammer/muted? true
             :jammer/volume 30}})

  (swap! re-frame.db/app-db assoc :room/jammers nil)
  (get @re-frame.db/app-db :room/jammers)

  (let [tp1 #uuid "39d04c2c-7214-5e2c-a9ae-32ff15405b7f"]
    (swap! app-db assoc-in [:teleporters tp1] {:teleporter/nickname "My test teleporter"
                                               :teleporter/id tp1}))

  )
