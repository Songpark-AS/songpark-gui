(ns web-app.forms.adapters
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]))


(defn text-adapter [{:keys [element] :as field}]
  (let [f (r/adapt-react-class element)]
    (log/debug ::adapat)
    (fn [{:keys [model placeholder value style] :as data}]
      (log/debug ::adapt-fn)
      [f (merge {:default-value value
                 :placeholder placeholder
                 :style style
                 :on-change #(reset! model (-> % .-target .-value))}
                (select-keys data [:id :rows]))])))
