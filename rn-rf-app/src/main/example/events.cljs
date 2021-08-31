(ns example.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [clojure.spec.alpha :as s]
   [example.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :inc-counter
 validate-spec
 (fn [db [_ _]]
   (update db :counter inc)))

(reg-event-db
 :set-view
 validate-spec
 (fn [db [_ view-name]]
   (assoc db :view view-name)))

(reg-event-db
 :set-balance
 validate-spec
 (fn [db [_ balance]]
   (assoc-in db [:studio :balance] balance)))

(reg-event-db
 :set-balance-slider
 validate-spec
 (fn [db [_ balance]]
   (prn (str "set-balance-slider event: " balance))
   (assoc-in db [:studio :balance-slider] balance)))

(reg-event-db
 :set-balance-sliding
 validate-spec
 (fn [db [_ is-sliding]]
   (assoc-in db [:studio :balance-sliding] is-sliding)))
