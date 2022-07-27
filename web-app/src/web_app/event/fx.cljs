(ns web-app.event.fx
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [web-app.subs.util :refer [get-input-kw
                                       get-tp-id]]
            [web-app.utils :refer [get-api-url get-platform-url]]))

(def fx-ks [:gate :reverb :amplify :equalizer :echo :compressor])
(def fx-type-ks (mapv #(keyword "fx" %) fx-ks))

(rf/reg-event-fx
 :fx.preset/delete
 (fn [{:keys [db]} [_ preset-id]]
   {:dispatch [:http/delete
               (get-api-url "/fx")
               {:fx.preset/id preset-id}
               :fx.preset/deleted]}))

(rf/reg-event-db
 :fx.preset/deleted
 (fn [db [_ {:keys [fx.preset/id]}]]
   (let [presets (:fx/presets db)]
     (assoc db :fx/presets (remove (fn [preset]
                                     (= (:fx.preset/id preset) id))
                                   presets)))))

(defn get-fx-map
  "Get the generic FX map based on input"
  [data input fx-k fx-ks]
  (merge
   {:fx/type (keyword :fx fx-k)}
   (reduce (fn [out k]
             (assoc out
                    (keyword (str "fx." (name fx-k)) k)
                    (get data
                         (get-input-kw input (keyword fx-k k))
                         0)))
           {} fx-ks)))

(defmulti get-fx (fn [teleporter input fx-k] fx-k))
(defmethod get-fx :gate [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:threshold :attack :release]))
(defmethod get-fx :reverb [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:mix :damp :room-size]))
(defmethod get-fx :amplify [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:drive :tone]))
(defmethod get-fx :equalizer [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:low :medium-low :medium-high :high]))
(defmethod get-fx :echo [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:delay-time :level]))
(defmethod get-fx :compressor [teleporter input fx-k]
  (get-fx-map teleporter input fx-k [:threshold :ratio :attack :release]))
(defmethod get-fx :default [teleporter input fx-k]
  fx-k)

(defn get-input-fx-map
  "Get the specific FX map for an input"
  [fx input]
  (reduce (fn [fx-map [k v]]
            (if (= k :fx/type)
              (assoc fx-map k v)
              (let [new-ns (as-> k $
                             (namespace $)
                             (subs $ 3)
                             (str "fx." (name input) "." $))
                    new-n (name k)]
                (assoc fx-map
                       (keyword new-ns new-n)
                       v))))
          {} fx))

(defn- get-input-fxs-from-preset
  "Get the FXs from a preset and correlate them with a specific input"
  [input preset]
  (->> preset
       :fx/fxs
       (map #(get-input-fx-map % input))))

(defn- prepare-input-fxs-for-gui
  "Add GUI specific things for the input FX"
  [input fxs]
  (let [current-fxs (into {} (map (juxt :fx/type identity) fxs))]
    (dissoc
     (->> (reduce (fn [out fx-k]
                    (let [fx-type-k (keyword "fx" fx-k)
                          switch-k (keyword (str/join "." ["fx" (name input) (name fx-k)])
                                            "switch")]
                      ;; we add the switch key to everything
                      ;; if the fx-type-k (ie, :fx/gate etc) exists,
                      ;; we add it with a value of true,
                      ;; otherwise false
                      ;; an equalivent operation takes place on the Teleporter
                      (assoc-in out [fx-type-k switch-k] (contains? out fx-type-k))))
                  current-fxs fx-ks)
          (vals)
          (apply merge))
     :fx/type)))


(defn- get-active-fxs
  "Get currently active FXs from a specific input"
  [db input]
  (let [teleporter (-> db :teleporters first second)]
    (reduce (fn [out fx-k]
              (let [k (get-input-kw input (keyword fx-k :switch))]
                (if (true? (get teleporter k))
                  (conj out (get-fx teleporter input fx-k))
                  out)))
            [] fx-ks)))

(rf/reg-event-fx
 :fx.preset/save
 (fn [{:keys [db]} [_ input preset-name]]
   (let [fxs (get-active-fxs db input)]
     {:dispatch [:http/put
                 (get-api-url "/fx")
                 {:fx.preset/name preset-name
                  :fx/fxs fxs}
                 :fx.preset/saved]})))

(rf/reg-event-db
 :fx.preset/saved
 (fn [db [_ data]]
   (update-in db [:fx/presets] conj data)))

(rf/reg-event-fx
 :fx.preset/update
 (fn [{:keys [db]} [_ input preset-id]]
   (let [fxs (get-active-fxs db input)]
     {:dispatch [:http/post
                 (get-api-url "/fx")
                 {:fx.preset/id preset-id
                  :fx/fxs fxs}
                 :fx.preset/updated]})))

(defn preset-updated [db [_ {:keys [fx.preset/id] :as data}]]
  (let [tp-id (get-tp-id db nil)
        presets (mapv (fn [fx]
                        (if (= (:fx.preset/id fx) id)
                          data
                          fx))
                      (:fx/presets db))]
    (-> db
        (assoc :fx/presets presets)
        (assoc-in [:teleporters tp-id :fx.preset/changed?] false))))

(rf/reg-event-db
 :fx.preset/updated
 preset-updated)

(rf/reg-event-fx
 :fx.preset/set
 (fn [{:keys [db]} [_ input preset-id]]
   (let [preset (->> db
                     :fx/presets
                     (filter #(= (:fx.preset/id %) preset-id))
                     first)
         tp-id (get-tp-id db nil)]
     ;; (log/debug {:preset preset
     ;;             :tp-id tp-id})
     (when preset
       (let [fxs (get-input-fxs-from-preset input preset)
             gui-fxs-settings (prepare-input-fxs-for-gui input fxs)]
         ;; (log/debug {:fxs fxs
         ;;             :gui-fxs-settings gui-fxs-settings})
         {:db (-> db
                  (update-in [:teleporters tp-id] merge gui-fxs-settings)
                  (assoc-in [:teleporters tp-id :fx.preset/current] preset-id)
                  (assoc-in [:teleporters tp-id :fx.preset/changed?] false))
          :dispatch [:mqtt/send-message-to-teleporter tp-id {:message/type :fx.preset/set
                                                             :fx/fxs fxs}]})))))


(comment
  ;; get currently active fxs from a specific input
  (get-active-fxs @re-frame.db/app-db :input2)

  ;; get the input fxs (FXs from a preset that is turned into specific FX for an
  ;; input) from a loaded fx in the app db
  (let [input :input2
        preset (first (:fx/presets @re-frame.db/app-db))]
    (get-input-fxs-from-preset input preset))

  ;; prep the input fx for the GUI
  (let [input :input2
        preset (first (:fx/presets @re-frame.db/app-db))
        input-fxs (get-input-fxs-from-preset input preset)]
    (prepare-input-fxs-for-gui input input-fxs))

  (preset-updated (select-keys @re-frame.db/app-db [:fx/presets])
                  [:fx.preset/updated {:fx.preset/id 21
                                       :fx.preset/name "foobar"}])
  )
