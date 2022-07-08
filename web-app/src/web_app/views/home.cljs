(ns web-app.views.home
  (:require [reagent.core :as r]
            [web-app.components.knob :refer [knob]]))

(defn index []
  (r/with-let [model (r/atom 0)]
    [:div "home view"
     [knob {:value/max 58
            :value 0
            :model model
            :title "Gain"
            :skin "dark"
            :value-fn (fn [v]
                        (str "-" v " dB"))
            :overload? (r/atom false)
            :on-change println}]
     [:button
      {:on-click #(reset! model (rand-int 59))}
      "Press me"]]))
