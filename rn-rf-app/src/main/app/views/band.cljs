(ns app.views.band
  (:require ["react-native" :as rn]))


;; Here be band view
(def styles {:text {:color :white}})

(defn BandView []
  [:> rn/View
   [:> rn/Text {:style (:text styles)} "Hello BandView"]])
