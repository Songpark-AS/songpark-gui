(ns app.views.navbar
  (:require ["react-native" :as rn]
            [re-frame.core :as rf]
            ["@expo/vector-icons" :refer [FontAwesome]]))

;; Navbar component (bottom buttons)

(def styles
  {:navbar-container {:flex-direction :row
                      :align-items :space-between
                      :align-self :flex-end}
   :icon-text {:base {:color :white
                      :font-size 16}
               :active {:font-weight :bold
                        :color :cyan}}
   :icon {:base {:color :white}
          :active {:color :cyan}}
   :nav-btn {:base {:flex 1
                    :flex-direction :column
                    :justify-content :space-between
                    :align-items :center
                    :padding 16}
             :active {#_:background-color "#444444"
                      #_:border-radius 24}}})

(defn view-active? [view-key]
  (let [view (rf/subscribe [:view])]
    (= view-key @view)))

(defn get-style [view-key style-key]
  (let [active? (view-active? view-key)]
    (if active?
      (->> (get styles style-key)
           vals
           (into {}))
      (get-in styles [style-key :base]))))

(defn Navbar-btn [title fa-icon view-key]
  [:> rn/TouchableOpacity {:style (get-style view-key :nav-btn) :on-press #(rf/dispatch [:set-view view-key])}
   [:> FontAwesome {:name fa-icon :style (get-style view-key :icon) :size 26}]
   [:> rn/Text {:style (get-style view-key :icon-text)} title]])


(defn Navbar []
  [:> rn/View {:style (:navbar-container styles)}
   (Navbar-btn "Band" "home" :band)
   (Navbar-btn "Studio" "headphones" :studio)
   (Navbar-btn "Chat" "comments" :chat)
   (Navbar-btn "Library" "book" :library)])
