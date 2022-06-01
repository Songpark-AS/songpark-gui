(ns web-app.event.util)

(defn add-error-message [db error-message]
  (let [messages (:error/messages db)]
    (assoc db :error/messages (conj (take 99 messages) error-message))))
