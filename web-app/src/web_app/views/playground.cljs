(ns web-app.views.playground
  (:require ["antd" :refer [Button]]))

(defn index []
  [:div.playground
   [:p "here be playground"]
   [:> Button {:type "primary"} "I am button"]])
