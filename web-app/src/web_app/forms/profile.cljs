(ns web-app.forms.profile
  (:require ["antd" :refer [Input
                            Input.TextArea
                            Select]]
            [ez-wire.form :as form]
            [re-frame.core :as rf]
            [web-app.forms.adapters :refer [select-adapter
                                            text-adapter]])
  (:require-macros [ez-wire.form.macros :refer [defform]]))

(defform profileform
  {}
  [{:element Select
    :adapter select-adapter
    :name :profile.pronoun/id
    :source (rf/subscribe [:profile/pronouns])
    :source/id :profile.pronoun/id
    :source/title :profile.pronoun/name}
   {:element Input
    :placeholder "Name"
    :adapter text-adapter
    :name :profile/name
    :validation :profile/name}
   {:element Input.TextArea
    :placeholder "Bio"
    :adapter text-adapter
    :name :profile/bio}
   {:element Input
    :placeholder "Location"
    :adapter text-adapter
    :name :profile/location}])
