(ns example.views.library
  (:require
   ["react-native" :as rn]
   ))
;; Here be library view

(def styles {:text {:color :white}})

(defn LibraryView []
  [:> rn/View
   [:> rn/Text {:style (:text styles)} "Hello LibraryView"]]
  )
