(ns web-app.api)

(defn add-failure [app-db k error]
  (assoc-in app-db [:api/failure k] error))

(defn remove-failure [app-db k]
  (assoc-in app-db [:api/failure k] nil))
