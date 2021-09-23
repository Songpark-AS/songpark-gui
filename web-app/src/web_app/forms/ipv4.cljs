(ns web-app.forms.ipv4
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [ez-wire.form :as form]
   [ez-wire.form.helpers :refer [valid?]]
   ["@material-ui/core/Button$default" :as Button]
   ["@mui/material/TextField$default" :as TextField])
  (:require-macros [ez-wire.form.macros :refer [defform]]
                   [ez-wire.form.validation :refer [defvalidation]]))

(defvalidation :teleporter/ip "Invalid IP-address")

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
      [f (merge {:value @model
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

(defform form-ipv4
  {:branch/branching? true
   :branch/branches {:ip/dhcp? (fn [{:keys [value]}]
                                 (if (not (nil? value))
                                   (if (true? value)
                                     {:fields {:ip/address {:disabled true}
                                               :ip/subnet {:disabled true}
                                               :ip/gateway {:disabled true}}}
                                     {:fields {:ip/address {:disabled false}
                                               :ip/subnet {:disabled false}
                                               :ip/gateway {:disabled false}}})
                                   {:fields {:ip/address {:disabled false}
                                             :ip/subnet {:disabled false}
                                             :ip/gateway {:disabled false}}}))}}
  [{:element TextField
    :adapter text-adapter
    :placeholder "192.168.0.123"
    :label "IP address"
    :name :ip/address
    :validation :teleporter/ip}
   {:element TextField
    :adapter text-adapter
    :placeholder "255.255.255.0"
    :label "Subnet mask"
    :name :ip/subnet
    :validation :teleporter/ip}
   {:element TextField
    :adapter text-adapter
    :placeholder "192.168.0.1"
    :label "Gateway address"
    :name :ip/gateway
    :validation :teleporter/ip}
   {:element input-checkbox
    :label "Use DHCP?:"
    :name :ip/dhcp?}])

(defn ipv4-config [tp-id]
  (let [form (form-ipv4 {} nil)
        data-form (rf/subscribe [:ez-wire.form/on-valid (:id form)])]
    (fn []
      [:div
       [:h4 "Network settings"]
       [form/as-table {} form]
       [:> Button {:color "primary" :variant "contained" :disabled (not (valid? data-form))} "Save"]
       ])))
