(ns web-app.views.teleporter.pairing
  (:require ["antd" :refer [Button]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.components.icon :refer [backspace]]
            [web-app.history :refer [back]]))


(defn confirm-link []
  [:div.confirm-link.squeeze
   [:div.intro
    [:div.title "Confirm link"]
    [:div.slogan "Press the blinking button on your Teleporter."]]
   [:div.note
    {:on-click #(rfe/push-state :views.teleporter/pair)}
    "Don't see a blinking button?"]
   [:div.teleporter
    [:div.circle]
    [:img {:src "/img/teleporter-blinking-800x800.png"}]]])

(defn- get-serial [numbers]
  (apply str numbers))

(defn- row [event-fn start-position]
  [:div.row
   (doall
    (for [i (range start-position (+ start-position 3))]
      ^{:key [:input-number i]}
      [:div.input-number
       {:on-click (event-fn i)}
       i]))])

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
       [:div.inner
        (doall
         (for [i (range 4)]
           ^{:key [:number i]}
           [:div.number
            (if (= i @pos)
              {:class "active"})
            (nth @numbers i)]))]
       [:div.no-connect
        {:on-click #(rfe/push-state :views/room)}
        "Continue without connecting"]]

      [:div.pad
       [row update-numbers 7]
       [row update-numbers 4]
       [row update-numbers 1]
       [:div.row.last
        [:div.input-number
         {:on-click (update-numbers 0)}
         0]
        [:div.input-number.no-bg
         [backspace {:on-click backtrack}]]]]]


     [:div.back
      {:on-click #(back)}
      "Go back"]
     [:> Button
      {:type "primary"
       :on-click #(success-fn (get-serial @numbers))
       :disabled (not= @pos 3)}
      "Connect"]]))

(defn index []
  [:div.pairing.squeeze
   [:div.intro
    [:div.title "Link up Teleporter"]
    [:div.slogan "Enter the four-digit serial number printed on your Teleporter"]]
   [number-pad #(rf/dispatch [:teleporter/pair %])]])
