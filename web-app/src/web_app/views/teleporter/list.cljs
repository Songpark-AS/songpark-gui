(ns web-app.views.teleporter.list
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]
            [cljs.core.async :as async :refer [go chan <! >!]]
            [reitit.frontend.easy :as rfe]
            ["antd" :refer [List List.Item List.Item.Meta Checkbox Button]]
            ["@ant-design/icons" :refer [SettingFilled GlobalOutlined ApiOutlined]]
            [web-app.subs]
            [web-app.utils :refer [is-touch?]]))

;; Here be listview of teleporters

;; TODO: this should be from config
(def max-num-selected-teleporters 2)

(def event-chan (async/chan (async/sliding-buffer 1)))
(def timer-id (atom nil))
(def hit? (atom false))
(def moved? (atom false))
(def moved-id (atom nil))

(async/go-loop []
  ;; remove the comment from any of the debug logs if needed
  ;; put them back when you have debugged what's needed
  ;; the events can be coming quite fast, and so any IO, such
  ;; as the console, slows things down
  (let [{:keys [event/type f] :as event} (async/<! event-chan)]
    ;;(log/debug "Caught event in go-loop" event)
    (cond
      (= type :tap/long)
      (reset! timer-id (js/setTimeout (fn []
                                        ;;(log/debug "Long tap")
                                        (reset! hit? true)
                                        (reset! moved? false)
                                        (when f
                                          (f event))) 350))
      (= type :tap/short)
      (do (js/clearTimeout @timer-id)
          (if (and (false? @hit?)
                   (false? @moved?))
            (do                                             ;;(log/debug "Short tap")
              (when f
                (f event))
              (reset! moved? false)
              (reset! hit? false))
            (reset! hit? false)))

      (= type :tap/cancel)
      (when (false? @moved?)
        (do (js/clearTimeout @timer-id)
            (reset! moved? true)
            (when-let [id @moved-id]
              (js/clearTimeout id))
            (reset! moved-id (js/setTimeout (fn []
                                              ;;(log/debug "Clear tap")
                                              (reset! moved? false))
                                            300))
            ;;(log/debug "Cancel tap")
            ))


      :else
      nil))
  (recur))

(defn- short-tap [event f]
  (async/put! event-chan (assoc event
                                :event/type :tap/short
                                :f f)))

(defn- long-tap [event f]
  (async/put! event-chan (assoc event
                                :event/type :tap/long
                                :f f)))

(defn- cancel-tap [event]
  (async/put! event-chan (assoc event
                                :event/type :tap/cancel)))


(defn- handle-short-tap [{:teleporter/keys [id] :as teleporter}]
  (let [tp-list-selection-mode @(rf/subscribe [:tp-list-selection-mode])
        selected-teleporters-staging @(rf/subscribe [:selected-teleporters-staging])
        teleporter-selected? (not (empty? (filter #(= (:teleporter/id %) id) selected-teleporters-staging)))]
    (when
        (and (true? tp-list-selection-mode)
             (or
              (and (>= (count selected-teleporters-staging) max-num-selected-teleporters) (true? teleporter-selected?))
              (< (count selected-teleporters-staging) max-num-selected-teleporters)))
      (if teleporter-selected?
        ;; tp is selected, we should unselect it
        (rf/dispatch [:set-selected-teleporters-staging (filter #(not (= (:teleporter/id %) id)) selected-teleporters-staging)])
        ;; tp is not selected, we should select it
        (rf/dispatch [:set-selected-teleporters-staging (conj selected-teleporters-staging teleporter)])))))

(defn- handle-long-tap [{:teleporter/keys [id]}]
  (.vibrate js/window.navigator 50)
  (rf/dispatch [:set-tp-list-selection-mode true]))


(defn handle-selection-action-cancel []
  (let [selected-teleporters @(rf/subscribe [:selected-teleporters])]
    (rf/dispatch [:set-selected-teleporters-staging selected-teleporters]))
  (rf/dispatch [:set-tp-list-selection-mode false]))

(defn handle-selection-action-select []
  (let [selected-teleporters-staging @(rf/subscribe [:selected-teleporters-staging])]
    (rf/dispatch [:set-selected-teleporters (->> selected-teleporters-staging
                                                 (map (juxt :teleporter/id identity))
                                                 (into {}))]))
  (rfe/push-state :views/jam)
  (rf/dispatch [:set-tp-list-selection-mode false]))

(defn selection-action-bar []
  (let [selected-teleporters-staging @(rf/subscribe [:selected-teleporters-staging])
        num-selected-teleporters (count selected-teleporters-staging)]
    [:div.selection-action-bar
     [:span (str num-selected-teleporters "/" max-num-selected-teleporters " selected")]
     [:div.actions 
      [:> Button {:type "primary"
                  :on-click #(handle-selection-action-select)
                  :disabled (not (> num-selected-teleporters 1))}
       "Confirm"]
      [:> Button {:on-click #(handle-selection-action-cancel)}
       "Cancel"]]]))

(defn- jamming? [{:keys [jam/status]}]
  (= status :jamming))

(defn teleporter-row [teleporter]
  (let [id (:teleporter/id teleporter)
        online? (rf/subscribe [:teleporter/online? id])
        jam-status (rf/subscribe [:teleporter/jam-status id])]
    [:> List.Item {:class-name "teleporter-row"
                   :actions [(r/as-element
                              [:a {:key (str id "-config-link")
                                   :href (rfe/href :views/teleporter {:id id})}
                               [:> SettingFilled
                                {:style {:font-size "1.2rem"}}]])]}
     [:> Button {:type "primary"
                 :on-click #(if (jamming? @jam-status)
                              (rf/dispatch [:jam/stop-by-teleporter-id id])
                              (rf/dispatch [:jam/ask id]))}
      (if (jamming? @jam-status)
        "Stop jam"
        "Join a jam")]
     [:span (str @jam-status)]
     (when (jamming? @jam-status)
       [:span ])
     [:> List.Item.Meta {:title (r/as-element [:span
                                               (if @online?
                                                 [:> GlobalOutlined {:className "tp-online-icon"}]
                                                 [:> ApiOutlined {:className "tp-offline-icon"}])
                                               (:teleporter/nickname teleporter)])
                         :description (r/as-element [:span (:teleporter/mac teleporter) ])}]]))

(defn list-component []
  (let [items (rf/subscribe [:teleporters])]
    (fn []
      [:> List
       (for [item @items]
	 (r/as-element ^{:key (:teleporter/id item)} [teleporter-row item]))])))


(defn index []
  (let [tp-list-selection-mode @(rf/subscribe [:tp-list-selection-mode])
        touch? (is-touch?)]
    [:div.teleporter-list-view
     [:h1 "Teleporter list"]
     [:p (str (if touch? "Long-press" "Double-click") " a row to select teleporters")]
     (when (true? tp-list-selection-mode)
       [selection-action-bar])
     [list-component]]))
