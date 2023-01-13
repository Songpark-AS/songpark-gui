(ns web-app.forms.adapters
  (:require ["antd" :refer [Select.Option]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log]
            [web-app.utils :refer [kw->str
                                   str->kw]]))


(defn text-adapter [{:keys [element] :as field}]
  (let [f (r/adapt-react-class element)]
    (fn [{:keys [model placeholder value style] :as data}]
      [f (merge {:default-value value
                 :placeholder placeholder
                 :style style
                 :on-change #(reset! model (-> % .-target .-value))}
                (select-keys data [:id :rows]))])))

(defn select-adapter [{:keys [element keywordize?] :as field}]
  (let [f (r/adapt-react-class element)]
    (fn [{:keys [name model value source source/id source/title] :as data}]
      [f {:default-value (if keywordize?
                           (kw->str value)
                           value)
          :on-change #(reset! model (if keywordize?
                                      (str->kw %)
                                      %))
          :filter-option false}
       (when source
         [:<>
          (doall
           (for [option @source]
             ^{:key [name (id option)]}
             [:> Select.Option {:value (if keywordize?
                                         (kw->str (id option))
                                         (id option))}
              (title option)]))])])))
