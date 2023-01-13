(ns web-app.forms.profile
  (:require ["antd" :refer [Input
                            Select]]
            [ez-wire.form :as form]
            [re-frame.core :as rf]
            [web-app.forms.adapters :refer [text-adapter]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))

(defform profileform
  {}
  [{:element Input
    :placeholder "Name"
    :adapter text-adapter
    :name :profile/name
    :validation :profile/name}
   {:element Input
    :placeholder "Position"
    :adapter text-adapter
    :name :profile/position}])
