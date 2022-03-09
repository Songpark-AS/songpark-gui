(ns web-app.subs.util)

(defn get-selected-teleporter [db]
  (let [tp-id (or (get db :teleporter.view/selected-teleporter)
                  (->> db
                       :teleporters
                       (vals)
                       (sort-by :teleporter/nickname)
                       first
                       :teleporter/id))]
    (get-in db [:teleporters tp-id])))
