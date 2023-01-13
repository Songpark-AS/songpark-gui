(ns web-app.forms.login
  (:require ["antd" :refer [Input
                            Input.Password]]
            [ez-wire.form :as form]
            [web-app.forms.adapters :refer [text-adapter]]
            [web-app.forms.validation])
  (:require-macros [ez-wire.form.macros :refer [defform]]))

(defform loginform
  {}
  [{:element Input
    :placeholder "Email"
    :adapter text-adapter
    :validation :auth.user/email
    :name :auth.user/email}
   {:element Input.Password
    :placeholder "Password"
    :adapter text-adapter
    :validation :auth.user/password
    :name :auth.user/password}])
