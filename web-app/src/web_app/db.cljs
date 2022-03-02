(ns web-app.db
  (:require [re-frame.db :refer [app-db]])
  (:refer-clojure :exclude [get-in]))

(def default-db
  {:teleporters []
   :selected-teleporters []
   :selected-teleporters-staging []
   :jam/started? false
   :tp-list-selection-mode false
   :view.log/level :info})


(defn get-in
  "Get the value from the re-frame app-db. Using this function bypasses the reactive nature (which is not always needed)"
  [path]
  (clojure.core/get-in @app-db path))
