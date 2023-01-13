(ns web-app.forms.signup
  (:require ["antd" :refer [Input
                            Input.Password]]
            [ez-wire.form :as form]
            [web-app.forms.adapters :refer [text-adapter]]
            [web-app.forms.validation :refer [auth-signup-same-password]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))


(defform signupform
  {}
  [{:element Input
    :placeholder "Name"
    :adapter text-adapter
    :name :profile/name
    :validation :profile/name}
   {:element Input
    :placeholder "Email address"
    :adapter text-adapter
    :name :auth.user/email
    :validation :auth.user/email}
   {:element Input.Password
    :placeholder "Password"
    :adapter text-adapter
    :name :auth.user/password
    :validation :auth.user/password}
   {:element Input.Password
    :placeholder "Confirm password"
    :adapter text-adapter
    :name :auth.user/repeat-password
    :validation auth-signup-same-password}])


(defform verifyemailform
  {}
  [{:element Input
    :placeholder "Token"
    :adapter text-adapter
    :name :auth.user/token}])
