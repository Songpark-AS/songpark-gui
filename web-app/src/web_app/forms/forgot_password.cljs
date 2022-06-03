(ns web-app.forms.forgot-password
  (:require ["antd" :refer [Input
                            Input.Password]]
            [ez-wire.form :as form]
            [web-app.forms.adapters :refer [text-adapter]]
            [web-app.forms.validation :refer [auth-reset-same-password]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))

(defform fwform
  {}
  [{:element Input
    :placeholder "Email"
    :adapter text-adapter
    :name :auth.user/email
    :validation :auth.user/email}])

(defform passwordform
  {}
  [{:element Input
    :placeholder "Token"
    :adapter text-adapter
    :name :auth.user/token}
   {:element Input.Password
    :placeholder "New password"
    :adapter text-adapter
    :name :auth.user/new-password
    :validation :auth.user/password}
   {:element Input.Password
    :placeholder "Repeat password"
    :adapter text-adapter
    :name :auth.user/repeat-password
    :validation auth-reset-same-password}])

(defform changepasswordform
  {}
  [{:element Input.Password
    :placeholder "Current password"
    :adapter text-adapter
    :name :auth.user/password}
   {:element Input.Password
    :placeholder "New password"
    :adapter text-adapter
    :name :auth.user/new-password
    :validation :auth.user/password}
   {:element Input.Password
    :placeholder "Repeat password"
    :adapter text-adapter
    :name :auth.user/repeat-password
    :validation auth-reset-same-password}])
