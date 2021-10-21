(ns web-app.views.jam
  (:require ["antd" :refer [Button Slider]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.api :refer [send-message!]]
            [web-app.mqtt :as mqtt]
            [web-app.utils :refer [scale-value]]))

;; Here be jam view
;; Select two teleporters with views.teleporter.list

;; This view will contain
;; View controls of both teleporters
;; Start/stop jam buttons


(def styles {:teleporters-container {:display :flex
                                     :flex-direction :row
                                     :align-items :center
                                     :justify-content :space-around}})

(def times (atom ()))
(def last-time (atom 0))
;;(def topic "d7d52eea-c597-4a90-bd4d-abfad567075d")

(defn on-global-volume-change [global tp-id value]
  (log/debug ::on-global-volume-change {:tp-id tp-id
                                        :value value})
  (reset! global value)
  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (send-message! {:message/type :teleporter.cmd/global-volume
                    :message/body {:teleporter/id tp-id
                                   :teleporter/volume value}})
    (reset! last-time (system-time))))

(defn on-local-volume-change [local tp-id value]
  (log/debug ::on-local-volume-change {:tp-id tp-id
                                       :value value})

  (reset! local value)
  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (send-message! {:message/type :teleporter.cmd/local-volume
                    :message/body {:teleporter/id tp-id
                                   :teleporter/volume value}})
    (reset! last-time (system-time))))

(defn on-network-volume-change [network tp-id value]
  (log/debug ::on-network-volume-change {:tp-id tp-id
                                         :value value})

  (reset! network value)
  (swap! times conj (system-time))
  (when (> (- (system-time) @last-time) 50)
    (send-message! {:message/type :teleporter.cmd/network-volume
                    :message/body {:teleporter/id tp-id
                                   :teleporter/volume value}})
    (reset! last-time (system-time))))


(defn- tp-status [global local network]
  [:<>
   [:h3 "Status"]
   [:div.tp-status
    [:div "Global volume " @global]
    [:div "Local volume" @local]
    [:div "Network volume " @network]]])

(defn tp-volume [header uuid value on-change]
  (r/with-let [started? (rf/subscribe [:jam/started?])]
    [:<>
     [:h3 header]
     [:> Slider {:style (:slider styles)
                 :min 0
                 :max 50
                 :step 1
                 :disabled (not @started?)
                 :value @value
                 :on-change #(on-change value uuid %)}]]))


(defn tp-panel [{:teleporter/keys [uuid nickname]}]
  (r/with-let [global (r/atom 50)
               network (r/atom 50)
               local (r/atom 50)]
    [:div.tp-panel {:key (str "tp-panel-" uuid)}
     [:h2 nickname]
     [tp-status global local network]
     [tp-volume "Global volume" uuid global on-global-volume-change]
     [tp-volume "Local volume" uuid local on-local-volume-change]
     [tp-volume "Network volume" uuid network on-network-volume-change]]))

(defn start-jam []
  (let [selected-teleporters @(rf/subscribe [:selected-teleporters])]
    (rf/dispatch [:start-jam (mapv #(select-keys % [:teleporter/uuid]) (vals selected-teleporters))])))

(defn stop-jam [jam]
  (log/debug ::stop-jam jam)
  (rf/dispatch [:stop-jam (:jam/uuid jam)]))

(defn jam-controls []
  (let [jam (rf/subscribe [:jam])
        started? (rf/subscribe [:jam/started?])]
    [:div.jam-controls
     (if (not (true? @started?))
       [:> Button {:type "primary" :on-click #(start-jam)} "Start jam"]
       [:> Button {:type "danger" :on-click #(stop-jam @jam) } "Stop jam"])]))


(defn index []
  (let [selected-teleporters (rf/subscribe [:selected-teleporters])
        jam (rf/subscribe [:jam])
        started? (rf/subscribe [:jam/started?])
        num-selected-teleporters (count (keys @selected-teleporters))]
    [:div.jam-view
     [:h1 "Jam view"]
     (when (>= num-selected-teleporters 2)
       [jam-controls])
     (if (> num-selected-teleporters 0)
       ;; (map tp-panel @selected-teleporters)
       (for [[_ tp] @selected-teleporters]
         [tp-panel tp]
         )
       [:p "No teleporters selected, select teleporters "
        [:a {:href (rfe/href :views/teleporters)} "here"]])]))
