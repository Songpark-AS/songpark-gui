(ns web-app.forms.ipv4
  (:require ["antd" :refer [Button Input Checkbox Alert]]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [taoensso.timbre :as log])
  (:require-macros [ez-wire.form.macros :refer [defform]]
                   [ez-wire.form.validation :refer [defvalidation]]))

(defvalidation :teleporter/ip
  (fn [_]
    [:> Alert {:type "error"
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

(defform form-ipv4
  {}
  [{:element Input
    :adapter text-adapter
    :placeholder "192.168.0.123"
    :label "IP address"
    :name :ip/address
    :validation :teleporter/ip}
   {:element Input
    :adapter text-adapter
    :placeholder "255.255.255.0"
    :label "Subnet mask"
    :name :ip/subnet
    :validation :teleporter/ip}
   {:element Input
    :adapter text-adapter
    :placeholder "192.168.0.1"
    :label "Gateway address"
    :name :ip/gateway
    :validation :teleporter/ip}])

(defn ipv4-config [tp-id network-config]
  (let [form (form-ipv4 {} network-config)
        data-form (rf/subscribe [:ez-wire.form/on-valid (:id form)])]
    [:div.ipv4-form
     ;; [:pre (pr-str @data-form)]
     ;; [:pre (pr-str @(:data form))]
     ;; [:pre (pr-str @network-config)]
     [form/as-table {} form]
     [:> Button {:type "primary"
                 :on-click #(rf/dispatch [:teleporter/save-ipv4
                                          tp-id
                                          (assoc @data-form
                                                 :ip/dhcp? false)])}
      "Save"]]))
