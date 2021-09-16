(ns web-app.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [web-app.events :as events]
   [web-app.views :as views]
   [web-app.config :as config]
   [web-app.init :as init]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn start []
  (dev-setup)
  (mount-root))

(defn init []
  (init/init {})
  (start))
