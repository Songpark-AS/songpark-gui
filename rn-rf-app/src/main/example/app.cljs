(ns example.app
  (:require
   ["react-native" :as rn]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [shadow.expo :as expo]
   [example.events]
   [example.subs]
   [example.navbar :refer [Navbar]]

   [example.views.band :refer [BandView]]
   [example.views.studio :refer [StudioView]]
   [example.views.chat :refer [ChatView]]
   [example.views.library :refer [LibraryView]]

   [example.mqtt :as mqtt]))

;; must use defonce and must refresh full app so metro can fill these in
;; at live-reload time `require` does not exist and will cause errors
;; must use path relative to :output-dir

(def view-map {
               :band BandView
               :studio StudioView
               :chat ChatView
               :library LibraryView
               })

(defn main-view []
  (let [view (rf/subscribe [:view])]
    (@view view-map)
    ))

(defonce splash-img (js/require "../assets/shadow-cljs.png"))
(def styles
  {:container   {:flex             1
                 :background-color "#070709"
                 :align-items      :center
                 :justify-content  :space-between
                 :padding-top      50}
   :tmptext       {:font-weight :normal
                 :font-size   15
                 :color       :white}})

(defn root []
  [:> rn/View {:style (:container styles)}
   [(main-view)]
   [Navbar]
   ])
(defn start
  {:dev/after-load true}
  []
  (expo/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (mqtt/connect)
  (start))
