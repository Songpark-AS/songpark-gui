(ns web-app.components.filler
  (:require [reagent.core :as r]))

(defn filler []
  (let [element (atom nil)
        trigger (r/atom -2)]
    (fn []
      [:div.filler
       {:ref (fn [el]
               (when (and el
                          (.-matches (js/window.matchMedia "only screen and (max-width: 500px)")))
                 (let [offset (.. el getBoundingClientRect -top)]
                   (set! (.. el -style -height) (str "calc(100vh - 100px - " offset "px)")))))}])))
