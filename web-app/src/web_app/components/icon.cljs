(ns web-app.components.icon
  (:refer-clojure :exclude [sort]))

(defn room []
  [:span.material-symbols-outlined "language"])
(defn levels []
  [:span.material-symbols-outlined "tune"])
(defn input []
  [:span.material-symbols-outlined "input"])
(defn backspace
  ([]
   [:span.material-symbols-outlined "backspace"])
  ([props]
   [:span.material-symbols-outlined props "backspace"]))
(defn account []
  [:span.material-symbols-outlined "account_circle"])
(defn delete []
  [:span.material-symbols-outlined "delete"])
(defn add []
  [:span.material-symbols-outlined "add"])
(defn sort []
  [:span.material-symbols-outlined "sort"])
(defn add-circle
  ([]
   [:span.material-symbols-outlined "add_circle"])
  ([props]
   [:span.material-symbols-outlined props "add_circle"]))
(defn arrow-right-alt []
  [:span.material-symbols-outlined "arrow_right_alt"])
(defn arrow-left-alt []
  [:span.arrow-left.material-symbols-outlined "arrow_right_alt"])
(defn edit []
  [:span.material-symbols-outlined "edit"])
(defn close []
  [:span.material-symbols-outlined "close"])
(defn cancel []
  [:span.material-symbols-outlined "cancel"])
(defn link []
  [:span.material-symbols-outlined "link"])
(defn check []
  [:span.material-symbols-outlined "check"])
(defn check-circle []
  [:span.material-symbols-outlined "check_circle"])

(defn logo []
  [:svg
   {:version "1.1"
    :id "logo"
    :viewBox "0 0 274.1 225.83"}
   [:g
    [:g
     [:polygon {:class "st0"
                :points "216.6,60.61 216.6,132.08 274.1,96.34"}]]
    [:g
     [:circle {:class "st0"
               :cx "115.38"
               :cy "94.58"
               :r "26.71"}]]
    [:g
     [:path {:class "st0"
             :d "M134.06,0H59.75C26.75,0,0,26.75,0,59.75v55.98l28.49,16.19V59.75c0-17.24,14.02-31.26,31.26-31.26h74.32 c17.24,0,31.26,14.02,31.26,31.26v149.9l28.49,16.19V59.75C193.81,26.75,167.06,0,134.06,0z"}]]]])

(defn logo+slogan []
  [:<>
   [:div.logo-wrapper
    [logo]]
   [:div.intro
    [:div.title "songpark"]
    [:div.slogan "never play alone"]]])
