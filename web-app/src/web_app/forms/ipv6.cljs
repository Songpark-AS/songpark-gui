(ns web-app.forms.ipv6
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [ez-wire.form :as form]
   [taoensso.timbre :as log]
   [ez-wire.form.helpers :refer [valid?]]
   ["antd" :refer [Button Input Checkbox Alert]]
   )
  (:require-macros [ez-wire.form.macros :refer [defform]]
                   [ez-wire.form.validation :refer [defvalidation]]))

(defvalidation :teleporter/ipv6 (fn [_] [:> Alert {:type "error"
                                                 :message "Invalid IP-address"
                                                 :showIcon true}]))



(defn text-adapter [{:keys [element] :as field}]
  (let [f (r/adapt-react-class element)]
    (fn [{:keys [model value label placeholder disabled] :as data}]
      [f (merge {:value @model
                 :placeholder placeholder
                 :label label
                 :disabled disabled
                 :on-change #(reset! model (-> % .-target .-value))}
                (select-keys data [:id]))])))


(defn checkbox-adapter [{:keys [element] :as field}]
  (let [f (r/adapt-react-class element)]
    (fn [{:keys [model] :as data}]
      [f (merge {:checked @model
                 :on-change #(reset! model (-> % .-target .-checked))}
                (select-keys data [:id]))])))

(defn input-text [{:keys [model placeholder disabled]}]
  [:input.input {:type :text
                 :placeholder placeholder
                 :value @model
                 :disabled disabled
                 :on-change #(reset! model (-> % .-target .-value))}])

(defn input-checkbox [{:keys [model]}]
  [:input.input {:type :checkbox
                 :value @model
                 :on-change #(reset! model (-> % .-target .-checked))}])

(defform form-ipv6
  {:branch/branching? true
   :branch/branches {:ip/autoconfigure? (fn [{:keys [value]}]
                                 (if (not (nil? value))
                                   (if (true? value)
                                     {:fields {:ip/address {:disabled true}
                                               :ip/gateway {:disabled true}}}
                                     {:fields {:ip/address {:disabled false}
                                               :ip/gateway {:disabled false}}})
                                   {:fields {:ip/address {:disabled false}
                                             :ip/gateway {:disabled false}}}))}}

  [{:element Input
    :adapter text-adapter
    :placeholder "2001:db8:85a3::8a2e:370:7334"
    :label "IP address"
    :name :ip/address
    :validation :teleporter/ipv6}
   {:element Input
    :adapter text-adapter
    :placeholder "fe80::1"
    :label "Gateway address"
    :name :ip/gateway
    :validation :teleporter/ipv6}
   {:element Checkbox
    :adapter checkbox-adapter
    :label "Autoconfigure?"
    :name :ip/autoconfigure?}
   ])

(defn ipv6-config [tp-id]
  (let [form (form-ipv6 {} nil)
        data-form (rf/subscribe [:ez-wire.form/on-valid (:id form)])]
    (fn [tp-id]
      [:div.ipv6-form
       ;; [:pre (pr-str @data-form)]
       ;; [:pre (pr-str @(:data form))]
       [form/as-table {} form]
       [:> Button {:type "primary" :disabled (not (or (:ip/autoconfigure? @(:data form))
                                                      (valid? data-form)))} "Save"]
       ])))

(comment
"
WIP outline of the form:
Types (select):
  Autoconfigure
  manual

Fields:
IP address (the ipv6 address of the teleporter)
Gateway address (the ipv6 address of the gateway)
Save
"

  )
