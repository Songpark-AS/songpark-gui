(ns app.views
  (:require ["react-native" :as rn]
            [re-frame.core :as rf]
            [app.views.navbar :refer [Navbar]]
            [app.views.band :refer [BandView]]
            [app.views.studio :refer [StudioView]]
            [app.views.chat :refer [ChatView]]
            ["react-native-safe-area-context" :refer [SafeAreaView]]
            ["expo-status-bar" :refer [StatusBar]]
            [app.views.library :refer [LibraryView]] ))

(def view-map {:band BandView
               :studio StudioView
               :chat ChatView
               :library LibraryView})

(defn main-router []
  (let [view (rf/subscribe [:view])]
    (@view view-map)))

(def styles
  {:safearea {:flex 1
              :background-color "#070709"
              }
   :container {:flex 1
               :align-items :center
               :justify-content :space-between
               :display :flex
               :flex-direction :column
               :background-color "#070709"
               }
   })

(defn root []
  [:> SafeAreaView {:style (:safearea styles)}
   [:> StatusBar {:style "light"}]
   [:> rn/View {:style (:container styles)}
   [(main-router)]
   [Navbar]]])
