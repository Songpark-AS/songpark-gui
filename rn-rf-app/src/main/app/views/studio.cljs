(ns app.views.studio
  (:require ["react-native" :as rn]
            ["@draftbit/ui" :refer [ButtonSolid CircleImage Icon ScreenContainer Slider]]
            [app.mqtt :as mqtt]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]))


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

#_(def client (mqtt/mqtt-client {:host "127.0.0.1"
                                 :port 8000
                                 :client-id "rn-rf-shadow-app"
                                 :store (atom {})}))

(defn handle-slider-value-change [value]
  #_(send! mqtt/client "World" (str {:balance value}))
  #_(mqtt/send! "World" (str {:balance value}))
  ;; (rf/dispatch [:set-balance value])
  )

(defn handle-on-sliding-start []
  (rf/dispatch [:set-balance-sliding true]))

(defn handle-on-sliding-complete [balance]
  (log/debug ::studio.on-sliding-complete "should set balance-slider to: " balance)
  (rf/dispatch [:set-balance-slider balance])
  (rf/dispatch [:set-balance-sliding false])
  )

(defn BalanceSlider []
  (let [balance-slider (rf/subscribe [:balance-slider])
        balance (rf/subscribe [:balance])]
    (fn []
      (log/debug ::studio.BalanceSlider "actual balance-slider: " @balance-slider)
      [:> rn/View {:style (:studio-container styles)}
       [:> rn/View {:style (:slider-container styles)}
        [:> rn/Text {:style (:balance-text styles)} "L"]
        #_[:> Slider {:style (:slider styles)
                    :minimumValue 0
                    :maximumValue 1
                    :minimumTrackTintColor "#FFFFFF"
                    :maximumTrackTintColor "#FFFFFF"
                    :thumbTintColor "#FFFFFF"
                    ;; :thumbImage splash-img
                    :onValueChange handle-slider-value-change
                    :onSlidingStart handle-on-sliding-start
                    :onSlidingComplete #(handle-on-sliding-complete @balance)
                    :value @balance-slider
                    }]
        [:> rn/Text {:style (:balance-text styles)} "R"]
        ]
       ]
      )
    )
  )

(defn StudioView []
  [:> rn/View {:style (:studio-container styles)}
   [BalanceSlider]]
  )

