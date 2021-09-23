(ns web-app.core
  (:require
   [reagent.dom :as rdom]
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [reitit.frontend.easy :as rfe]
   [web-app.events :as events]
   [web-app.views :as views]
   [web-app.config :as config]
   [web-app.init :as init]
   [web-app.routes :refer [routes router]]
   ))





(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init-router! []
  (rfe/start!
   router
   (fn [m] (reset! views/match m))
   {:use-fragment true}))

(defn start []
  (dev-setup)
  (init-router!)
  (mount-root))

(defn init []
  (init/init {})
  (start))
