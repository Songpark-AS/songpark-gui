(ns web-app.views.preset
  (:require [web-app.components.back :refer [back]]))

(defn index [matched]
  [:div
   [back :views/input1]
   "preset" (pr-str matched)])
