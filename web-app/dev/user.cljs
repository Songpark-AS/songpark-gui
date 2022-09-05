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

  @app-db

  (rf/dispatch [:app/init])

  (let [tp1 #uuid "39d04c2c-7214-5e2c-a9ae-32ff15405b7f"]
    (swap! app-db assoc-in [:teleporters tp1] {:teleporter/nickname "My test teleporter"
                                               :teleporter/id tp1}))

  )
