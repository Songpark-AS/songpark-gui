(ns web-app.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as log]
   ))

(defonce match (r/atom nil))

(defn main-panel []
  [:div
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   ])
