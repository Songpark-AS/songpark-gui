(ns web-app.auth
  "Utilty namespace for auth handling")

(defn logged-in? [user]
  (number? (:auth.user/id user)))

(defn logged-out? [user]
  (not (number? (:auth.user/id user))))
