(ns web-app.subs.teleporter
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [web-app.subs.util :refer [get-input-kw
                                       get-tp-id
                                       get-selected-teleporter]]))

(rf/reg-sub
 :teleporters
 (fn [db _]
   (->> db
        :teleporters
        (vals)
        (sort-by :teleporter/nickname))))

(rf/reg-sub
 :teleporter/paired?
 (fn [db _]
   (->> db
        :teleporters
        ffirst
        some?)))

(rf/reg-sub
 :teleporter/teleporter
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id]))))

(rf/reg-sub
 :teleporter/online?
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/online?]))))

(rf/reg-sub
 :teleporter/upgrading?
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (true? (get-in db [:teleporters tp-id :teleporter/upgrading?])))))

(rf/reg-sub
 :teleporter/net-config
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/net-config]))))

(rf/reg-sub
 :teleporter/coredump
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :jam/coredump]))))

(rf/reg-sub
 :teleporter/apt-version
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/apt-version]))))

(rf/reg-sub
 :teleporter/versions
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (select-keys (get-in db [:teleporters tp-id])
                  [:teleporter/fpga-version
                   :teleporter/bp-version
                   :teleporter/tpx-sha
                   :teleporter/tpx-version]))))

(rf/reg-sub
 :teleporter/latest-available-apt-version
 (fn [db _]
   (get db :teleporter/latest-available-apt-version)))

(rf/reg-sub
 :teleporter/upgrade-status
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/upgrade-status]))))


(rf/reg-sub
 :teleporter/offline-timeout
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/offline-timeout]))))

(rf/reg-sub
 :teleporter/upgrade-timeout
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)]
     (get-in db [:teleporters tp-id :teleporter/upgrade-timeout]))))

(rf/reg-sub
 :teleporter/jam-status
 (fn [db [_ tp-id]]
   (let [tp-id (get-tp-id db tp-id)
         status (get-in db [:teleporters tp-id :jam/status] :idle)
         stream (get-in db [:teleporters tp-id :jam/stream])
         sync (get-in db [:teleporters tp-id :jam/sync])
         syncing (get-in db [:teleporters tp-id :sync/syncing])]
     (assoc (select-keys (get-in db [:teleporters tp-id])
                         [:jam/stream
                          :jam/sync
                          :sync/syncing
                          :sync/end])
            :jam/status status))))

(rf/reg-sub
 :teleporter/setting
 (fn [db [_ tp-id tp-settings-k]]
   (let [tp-id (get-tp-id db tp-id)]
     (or (get-in db [:teleporters tp-id :teleporter/setting tp-settings-k])
         (get-in db [:teleporters tp-id tp-settings-k])))))


(defn sub-fx-fn [db [_ tp-id input fx-k]]
  (let [tp-id (get-tp-id db tp-id)]
    (get-in db [:teleporters tp-id (get-input-kw input fx-k)])))

(rf/reg-sub
 :teleporter/fx
 sub-fx-fn)

(rf/reg-sub
 :teleporter/active-inputs
 (fn [db [_ tp-id input]]
   (let [tp-id (get-tp-id db tp-id)]
     (->> [(if (get-in db [:teleporters tp-id (get-input-kw input :reverb/switch)])
             "Reverb")
           (if (get-in db [:teleporters tp-id (get-input-kw input :equalizer/switch)])
             "Equalizer")
           (if (get-in db [:teleporters tp-id (get-input-kw input :echo/switch)])
             "Echo")
           (if (get-in db [:teleporters tp-id (get-input-kw input :gate/switch)])
             "Gate")
           (if (get-in db [:teleporters tp-id (get-input-kw input :amplify/switch)])
             "Amplify")
           (if (get-in db [:teleporters tp-id (get-input-kw input :compressor/switch)])
             "Compressor")]
          (remove nil?)
          (str/join " ")))))


(comment
  @(rf/subscribe [:teleporter/active-inputs nil "input1"])
  (let [db @re-frame.db/app-db
        tp-id (get-tp-id db nil)
        input "input1"
        fx-k :compressor/switch]
    ;; (sub-fx-fn db [:teleporter/fx nil input :gate/attack])
    (get-in db [:teleporters tp-id (get-input-kw input fx-k)]))
  )
