(ns example.views.studio
  (:require
   ["react-native" :as rn]
   ["@react-native-community/slider$default" :as Slider]
   [example.mqtt :as mqtt]
   [re-frame.core :as rf]
   ))
;; Here be studio view


(def styles {:slider-container {:flex-direction :row
                                :justify-content :space-between
                                :align-items :center}
             :text {:color :white}
             :balance-text {:color :white
                            :font-size 24}
             :slider {:width 280
                      :height 40}
             :studio-container {:flex-direction :column
                                :align-items :center}})


;; (defonce splash-img (js/require "../assets/shadow-cljs.png"))

(defn handle-slider-value-change [value]
  (mqtt/send! "World" (str {:balance value}))
  ;; (rf/dispatch [:set-balance value])
  )

(defn handle-on-sliding-start []
  (rf/dispatch [:set-balance-sliding true])
  )

(defn handle-on-sliding-complete [balance]
  (prn (str "should set balance-slider to: " balance))
  (rf/dispatch [:set-balance-slider balance])
  (rf/dispatch [:set-balance-sliding false])
  )

(defn BalanceSlider []
  (let [balance-slider (rf/subscribe [:balance-slider])
        balance (rf/subscribe [:balance])]
    (fn []
      (prn (str "actual balance-slider: " @balance-slider))
      [:> rn/View {:style (:studio-container styles)}
       [:> rn/View {:style (:slider-container styles)}
        [:> rn/Text {:style (:balance-text styles)} "L"]
        [:> Slider {:style (:slider styles)
                    :minimumValue 0
                    :maximumValue 1
                    :minimumTrackTintColor "#FFFFFF"
                    :maximumTrackTintColor "#00FF00"
                    ;; :thumbImage splash-img
                    :onValueChange handle-slider-value-change
                    :onSlidingStart handle-on-sliding-start
                    :onSlidingComplete #(handle-on-sliding-complete @balance)
                    :value @balance-slider
                    }]
        [:> rn/Text {:style (:balance-text styles)} "R"]
        ]
       [:> rn/Text {:style (:text styles)} (str "Balance: " @balance)]
       ]
      )
    )
  )

(defn StudioView []
  [:> rn/View {:style (:studio-container styles)}
   [:> rn/Text {:style (:text styles)} "Hello StudioView"]
   [BalanceSlider]]
  )

