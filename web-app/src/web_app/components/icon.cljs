(ns web-app.components.icon
  (:refer-clojure :exclude [sort]))

(defn room []
  [:span.material-symbols-outlined "language"])
(defn levels []
  [:span.material-symbols-outlined "tune"])
(defn input []
  [:span.material-symbols-outlined "input"])
(defn backspace []
  [:span.material-symbols-outlined "backspace"])
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
