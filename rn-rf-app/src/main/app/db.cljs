(ns app.db)

(def default-db {:counter 0
                 :view :band
                 :studio {:audio/balance 0.5
                          :audio/balance-slider 0.5
                          :audio/balance-sliding false}})


;; (ns example.db
;;   (:require [clojure.spec.alpha :as s]))

;; ;; spec of app-db
;; (s/def ::counter number?)
;; (s/def ::app-db
;;   (s/keys :req-un [::counter]))

;; ;; initial state of app-db
;; (defonce app-db {:counter 0
;;                  :view :band
;;                  :studio {
;;                           :balance 0.5
;;                           :balance-slider 0.5
;;                           :balance-sliding false
;;                           }})
