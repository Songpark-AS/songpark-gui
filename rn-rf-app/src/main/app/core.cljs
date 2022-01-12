(ns app.core
  (:require ["react-native" :as rn]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [shadow.expo :as expo]
            [songpark.common.config :as config]
            [app.init :as init]

            [app.event]
            [app.subs]

            [app.views :refer [root]]

            [app.mqtt :as mqtt]))

;; must use defonce and must refresh full app so metro can fill these in
;; at live-reload time `require` does not exist and will cause errors
;; must use path relative to :output-dir
(defonce splash-img (js/require "../assets/splash.png"))


(defn start
  {:dev/after-load true}
  []
  (expo/render-root (r/as-element [root])))

(defn init []
  (init/init (js->clj @config/config :keywordize-keys true))
  #_(mqtt/connect) ;; this should be handled by MqttManager
  (start))
