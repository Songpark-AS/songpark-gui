(ns web-app.forms.room
  (:require ["antd" :refer [Input]]
            [ez-wire.form :as form]
            [web-app.forms.adapters :refer [text-adapter]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))



(defform roomform
  {}
  [{:element Input
    :placeholder "Name"
    :adapter text-adapter
    :name :room/name
    :validation :room/name}])
