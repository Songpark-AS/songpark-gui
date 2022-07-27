(ns web-app.subs.util
  (:require [clojure.string :as str]))

(defn get-selected-teleporter [db]
  (let [tp-id (or (get db :teleporter.view/selected-teleporter)
                  (->> db
                       :teleporters
                       (vals)
                       (sort-by :teleporter/nickname)
                       first
                       :teleporter/id))
        teleporter (get-in db [:teleporters tp-id])]
    teleporter))

(defn- get-tp-id [db tp-id]
  (if (nil? tp-id)
    (->> db
         :teleporters
         (vals)
         first
         :teleporter/id)
    tp-id))

(defn get-input-kw [input k]
  (let [input* (name input)
        ns* (namespace k)
        n* (name k)]
    (keyword (str/join "." (flatten (remove nil? ["fx" input* ns*])))
             n*)))
