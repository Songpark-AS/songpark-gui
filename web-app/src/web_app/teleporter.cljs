(ns web-app.teleporter
  (:require [re-frame.db :refer [app-db]]))


(defn paired? []
  (->> @app-db
       :teleporters
       first
       some?))
