(ns web-app.views
  (:require
   ["antd" :refer [Layout Layout.Content]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   [taoensso.timbre :as log]))

(defonce match (r/atom nil))

(defn main []
  [:> Layout
   [:> Layout.Content
    [:div.content-wrapper
     (if @match
       (let [view (:view (:data @match))]
         [view @match]))]]])

(defn main-panel []
  [:div.main-panel
   [:> Layout {:class-name "main-layout"}
    [main]]])
