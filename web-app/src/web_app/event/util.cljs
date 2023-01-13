(ns web-app.event.util)

(defn add-error-message [db error-message]
  (let [messages (:error/messages db)]
    (assoc db :error/messages (conj (take 99 messages) error-message))))


(defn message-base
  ([db]
   (message-base db nil #{:auth.user/id :teleporter/id}))
  ([db data]
   (message-base db data #{:auth.user/id :teleporter/id}))
  ([db data opts]
   (merge
    (as-> {} $
      (if (opts :auth.user/id)
        (assoc $ :auth.user/id (-> db :auth/user :auth.user/id)))
      (if (opts :teleporter/id)
        (assoc $ :teleporter/id (-> db :teleporters ffirst))))
    data)))


(comment
 (message-base @re-frame.db/app-db
               {:teleporter/id :foo})
 )
