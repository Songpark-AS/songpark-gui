(ns web-app.forms.settings
  (:require ["antd" :refer [Input]]
            [ez-wire.form :as form]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [web-app.forms.adapters :refer [text-adapter]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))


(defform settingsform
  {}
  [{:element Input
    :placeholder "Name"
    :adapter text-adapter
    :name :teleporter/nickname
    :label "Name"
    :validation :teleporter/nickname}])
