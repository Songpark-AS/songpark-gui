(ns example.navbar
  (:require
   ["react-native" :as rn]
   [re-frame.core :as rf]
   ["@expo/vector-icons" :refer [FontAwesome]]
   ))
;; Navbar component (bottom buttons)

(def styles
  {:navbar-container {:flex-direction :row
                      :align-items :space-between
                      :align-self :flex-end
                      :background-color "#070709"}
   :icon-text {:color :white
               :font-size 16
               :font-weight :bold}
   :nav-btn {:flex 1
             :flex-direction :column
             :justify-content :space-between
             :align-items :center
             :padding 20}})

(defn Navbar []
  [:> rn/View {:style (:navbar-container styles)}
   [:> rn/TouchableOpacity {:style (:nav-btn styles) :on-press #(rf/dispatch [:set-view :band])}
    [:> FontAwesome {:name "home" :color "rgb(222, 224, 224)" :size 26}]
    [:> rn/Text {:style (:icon-text styles)} "Band"]
    ]
   [:> rn/TouchableOpacity {:style (:nav-btn styles) :on-press #(rf/dispatch [:set-view :studio])}
    [:> FontAwesome {:name "headphones" :color "rgb(222, 224, 224)" :size 26}]
    [:> rn/Text {:style (:icon-text styles)} "Studio"]
    ]
   [:> rn/TouchableOpacity {:style (:nav-btn styles) :on-press #(rf/dispatch [:set-view :chat])}
    [:> FontAwesome {:name "comments" :color "rgb(222, 224, 224)" :size 26}]
    [:> rn/Text {:style (:icon-text styles)} "Chat"]
    ]
   [:> rn/TouchableOpacity {:style (:nav-btn styles) :on-press #(rf/dispatch [:set-view :library])}
    [:> FontAwesome {:name "book" :color "rgb(222, 224, 224)" :size 26}]
    [:> rn/Text {:style (:icon-text styles)} "Library"]
    ]
   ])
