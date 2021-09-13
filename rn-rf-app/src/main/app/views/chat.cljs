(ns app.views.chat
  (:require [clojure.string :as str]
            ["react-native" :as rn]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]
            [goog.object :as gobj]))

;; Here be chat view

(def styles {:text {:color :white
                    :padding-bottom 2}
             :pressable/status {:font-weight      :bold
                                :font-size        18
                                :padding          6
                                :background-color :blue
                                :margin 10
                                :border-radius    999
                                :width 156}
             :button-text {:padding-left 12
                           :padding-right 12
                           :font-weight :bold
                           :color :white
                           :font-size 18}
             :list/item {:color :white
                         :padding 2
                         :margin-vertical 4}
             :list/section {:color :grey
                            :font-size 24}
             :text/code {:color :green
                         :font-size 10}})


;; TODO: add tp nickname to platform response, currently hardcoded!
(defn- get-sections [{:mqtt/keys [username password]}
                     {:teleporter/keys [bits uuid nickname]}]
  [{:title "MQTT"
    :data [{:key "mqtt.username"
            :value (str "Username: " username)}
           {:key "mqtt.password"
            :value (str "Password: " password)}]}
   {:title "Teleporter"
    :data [{:key "tp.nickname"
            :value (str "Nickname: " nickname)}
           {:key "tp.bits"
            :value (str "Bits: " bits)}
           {:key "tp.uuid"
            :value (str "UUID: " uuid)}]}])

(defn- list-section [props]
  (r/as-element
   [:> rn/Text {:style (:list/section styles)}
    (gobj/getValueByKeys props "section" "title")]))

(defn- list-item [props]
  (r/as-element
   [:> rn/View {:style (:list/item styles)}
    [:> rn/Text {:style (:text styles)}
     (gobj/getValueByKeys props "item" "value")]]))

(defn ChatView []
  (let [mqtt (rf/subscribe [:mqtt/data])
        teleporter (rf/subscribe [:teleporter/data])]
    [:> rn/View {:style {:width "90%"
                         :display :flex
                         :flex-direction :column
                         :align-items :center
                         :justify-content :space-evenly}}
     [:> rn/Text {:style (:text styles)} "Hello ChatView"]
     [:> rn/Pressable {:style (:pressable/status styles)
                       :on-press #(rf/dispatch [:teleporter/status {:teleporter/nickname "christians.dream"}])}
      [:> rn/Text {:style (:button-text styles)} "Get TP status"]]     
     [:> rn/View {:style {:width "100%"
                          :padding-top 20
                          :display :flex
                          :flex-direction :row
                          :justify-items :flex-start
                          :align-items :center}}
      [:> rn/SectionList {:sections (get-sections @mqtt @teleporter)
                                    :render-item list-item
                                    :render-section-header list-section}]]]))


