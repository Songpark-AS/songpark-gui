(ns web-app.views.teleporter.pairing
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]))

(defn- get-serial [numbers]
  (apply str numbers))

(defn number-pad [success-fn]
  (r/with-let [pos (r/atom 0)
               numbers (r/atom [0 0 0 0])
               update-numbers (fn [idx]
                                (fn [_]
                                  (swap! numbers assoc @pos idx)
                                  (let [n @pos]
                                    (reset! pos (min (inc n) 3)))))
               backtrack (fn [_]
                           (swap! numbers assoc @pos 0)
                           (let [n @pos]
                             (reset! pos (max (dec n) 0))))]
    [:<>
     [:div.number-pad
      [:div.numbers
       (doall
        (for [i (range 4)]
          ^{:key [:number i]}
          [:div.number
           (if (= i @pos)
             {:class "active"})
           (nth @numbers i)]))]
      [:div.pad
       (doall
        (for [i (range 10)]
          ^{:key [:input-number i]}
          [:div.input-number
           {:on-click (update-numbers i)}
           i]))
       [:div.back
        {:on-click backtrack}
        "back"]]]
     [:> Button
      {:type "primary"
       :on-click #(success-fn (get-serial @numbers))
       :disabled (not= @pos 3)}
      "Connect"]
     [:p
      {:on-click #(rfe/push-state :views/home)}
      "Continue without connecting"]]))

(defn index []
  [:div.pairing
   [:h2 "Link up Teleporter"]
   [:p "Enter the four-digit serial number printed on your Teleporter"]
   [number-pad #(rf/dispatch [:teleporter/pair %])]])
