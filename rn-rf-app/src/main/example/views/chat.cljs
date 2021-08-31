(ns example.views.chat
  (:require
   ["react-native" :as rn]
   ))
;; Here be chat view

(def styles {:text {:color :white}})

(defn ChatView []
  [:> rn/View
   [:> rn/Text {:style (:text styles)} "Hello ChatView"]]
  )
