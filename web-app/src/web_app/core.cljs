(ns web-app.core
  (:require [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            ;; load config first, and after that init
            [web-app.config :as config]
            [web-app.init :as init]
            [web-app.routes :refer [routes router]]
            [web-app.views :as views]))





(defn dev-setup []
  (when config/debug?
    (log/info "DEV MODE")))

(defn init-router! []
  (rfe/start!
   router
   (fn [m] (reset! views/match m))
   {:use-fragment true}))

(defn ^:dev/after-load mount-root []
  (init-router!)
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))


(defn start []
  (dev-setup)
  (mount-root))

(defn init []
  (init/init {})
  (start))
