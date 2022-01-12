(ns web-app.events
  (:require
   [re-frame.core :as re-frame]
   [web-app.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [web-app.event.ui]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))
