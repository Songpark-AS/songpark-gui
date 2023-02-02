(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                          find-doc print-doc pst source)]
            [clojure.pprint :refer (pprint)]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [reitit.frontend.easy :as rfe]))



(comment

  (rfe/push-state :views.room.join/invite {:normalized-name "my-fantastic-room-2"})
  (rfe/push-state :views/room)
  (rfe/push-state :views.teleporter/confirm)

  (def serial0001 #uuid "39d04c2c-7214-5e2c-a9ae-32ff15405b7f")

  (swap! app-db assoc-in [:teleporters serial0001 :teleporter/tpx-version] "0.4")

  (swap! app-db dissoc :room/jam)

  (get-in @app-db [:teleporters])
  (get-in @app-db [:navigation/routes])
  (get-in @app-db [:navigation.room/routes])

  (get-in @app-db [:auth/user])
  (get-in @app-db [:room/jam :room/jammers])
  (get-in @app-db [:room/jam])
  (let [db @app-db]
    (-> db (update-in [:room/jam :room/jammers] dissoc 3)
        (get-in [:room/jam :room/jammers])))
  (get-in @app-db [:room/room])
  (get-in @app-db [:teleporters
                   #uuid "77756ff0-bb05-5e6a-b7d9-28086f3a07fd"
                   :jam/coredump])
  @(rf/subscribe [:room/jam])
  @(rf/subscribe [:room/people :owner])
  @(rf/subscribe [:room/people :knocking])

  @(rf/subscribe [:teleporter/jam-status #uuid "77756ff0-bb05-5e6a-b7d9-28086f3a07fd"])
  @(rf/subscribe [:teleporter/jam-status #uuid "39d04c2c-7214-5e2c-a9ae-32ff15405b7f"])
  @(rf/subscribe [:teleporter/coredump 1])

  (rf/dispatch [:app/init])
  (rf/dispatch [:room.jam/host 2])

  ;; for dev purposes
  (swap! app-db assoc-in [:room/jam :room/jammers]
         {1 {:profile/name "Emil Bengtsson"
             :auth.user/id 1
             :teleporter/id 1
             :profile/position "Vocals (deep bass)"}
          5 {:profile/name "Test McTest"
             :auth.user/id 5
             :jammer/status :jamming
             :teleporter/id 5
             :profile/position "The world's smallest violin"}
          6 {:profile/name "Christian Ruud"
             :auth.user/id 6
             :teleporter/id 6
             :profile/image-url "http://localhost:3000/static/images/christian.jpg"
             :profile/position "Guitar"
             :jammer/status :knocking
             :jammer/muted? true
             :jammer/volume 30}})
  (swap! app-db update-in [:teleporters]
         merge
         {1 {:jam/coredump {:Latency "2.45"}}
          5 {:jam/coredump {:Latency "5.43"}}
          6 {:jam/coredump {:Latency "1.24"}}})

  (swap! re-frame.db/app-db assoc :room/jammers nil)
  (get @re-frame.db/app-db :room/jammers)

  (let [tp1 #uuid "39d04c2c-7214-5e2c-a9ae-32ff15405b7f"]
    (swap! app-db assoc-in [:teleporters tp1] {:teleporter/nickname "My test teleporter"
                                               :teleporter/id tp1}))

  )
