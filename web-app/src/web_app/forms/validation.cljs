(ns web-app.forms.validation
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [songpark.taxonomy.auth])
  (:require-macros [ez-wire.form.validation :refer [defvalidation
                                                    defmultivalidation]]))


(defvalidation :auth.user/email
  "This is an incorrect email")

(defvalidation :auth.user/password
  "A password cannot be empty")

(defvalidation :profile/name
  "A profile name cannot be empty")

(defmultivalidation auth-signup-same-password
  #{:auth.user/password :auth.user/repeat-password}
  (fn [{:keys [values]}]
    (let [{:auth.user/keys [password repeat-password]} values]
      (= password repeat-password)))
  "Password and repeat password must be the same")

(defmultivalidation auth-reset-same-password
  #{:auth.user/new-password :auth.user/repeat-password}
  (fn [{:keys [values]}]
    (let [{:auth.user/keys [new-password repeat-password]} values]
      (= new-password repeat-password)))
  "Password and repeat password must be the same")
