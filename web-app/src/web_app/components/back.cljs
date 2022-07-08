(ns web-app.components.back
  (:require [reitit.frontend.easy :as rfe]))


(defn back
  ([]
   [:div.back
    {:on-click #(.back js/window.history)}
    [:span.material-symbols-outlined "arrow_right_alt"]])
  ([path]
   [:div.back
    {:on-click #(rfe/push-state path)}
    [:span.material-symbols-outlined "arrow_right_alt"]])
  ([path params]
   [:div.back
    {:on-click #(rfe/push-state path params)}
    [:span.material-symbols-outlined "arrow_right_alt"]]))


(defn back-fn [fn]
  [:div.back
   {:on-click fn}
   [:span.material-symbols-outlined "arrow_right_alt"]])
