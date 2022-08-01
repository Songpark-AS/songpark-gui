(ns web-app.views.profile
  (:require ["antd" :refer [Button
                            Image
                            Input]]
            [clojure.string :as str]
            [ez-wire.form :as form]
            [ez-wire.form.helpers :refer [add-external-error
                                          valid?]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]
            [web-app.components.icon :refer [add-circle
                                             arrow-left-alt
                                             edit]]
            [web-app.forms.profile :refer [profileform]]
            [web-app.history :refer [back]]))

(defn- get-file-type [mimetype]
  (if (= mimetype :unknown)
    :unknown
    (last (str/split mimetype #"/"))))

(defn- get-file-metadata [file-object]
  (let [mime (or (.-type file-object) :unknown)]
    {:file/name (or (.-name file-object) :unknown)
     :file/mime mime
     :file/type (get-file-type mime)
     :file/size (or (.-size file-object) :unknown)}))

(defn- trim-image-data [string-data]
  (let [index (cond (str/starts-with? string-data "data:image/png;base64,")
                    22
                    (str/starts-with? string-data "data:image/jpg;base64,")
                    22
                    (str/starts-with? string-data "data:image/jpeg;base64,")
                    23
                    :else
                    0)]
    (subs string-data index)))

(defn- read-image-data [blob file-object]
  (if (and (.-type file-object)
           (not (str/starts-with? (.-type file-object) "image/")))
    false
    (do (let [reader (js/FileReader.)]
          (.addEventListener reader
                             "load"
                             (fn [e]
                               (let [metadata (get-file-metadata file-object)
                                     image-base64 (trim-image-data (-> e .-target .-result))]
                                 (reset! blob {:file/base64 image-base64
                                               :file/type (:file/type metadata)}))))
          (.readAsDataURL reader file-object))
        true)))

(defn- show-form [profile-data]
  (r/with-let [f (profileform {:label? false} profile-data)
               form-data (rf/subscribe [::form/on-valid (:id f)])
               image-data (atom nil)
               handler (fn [data]
                         (rf/dispatch [:profile/set data]))
               error-handler (fn [{:keys [response]}]
                               (add-external-error f
                                                   (:error/key response)
                                                   (:error/key response)
                                                   (:error/message response)
                                                   true))
               event (fn [e]
                       (let [data @form-data
                             data (if @image-data
                                    (assoc data
                                           :profile.image/base64 (:file/base64 @image-data)
                                           :profile.image/type (:file/type @image-data))
                                    data)]
                         (when (valid? data)
                           (log/debug :data data)
                           (rf/dispatch [:profile/save
                                         data
                                         {:handler handler
                                          :error error-handler}]))))]
    [:<>
     [:div.profile-form.form
      [:h2
       {:on-click #(back)}
       [arrow-left-alt]
       "Profile"]
      [:form
       {:on-submit event}
       [:div.image
        [:input {:style {:display "none"}
                 :type :file
                 :accept ".png, .jpg, .jpeg"
                 :on-change (fn [e]
                              (let [files (-> e .-target .-files)
                                    file (if (> (.-length files) 0)
                                           (aget files 0)
                                           nil)]
                                (read-image-data image-data file)))
                 :id "profile-image"}]
        (if (:profile/image-url profile-data)
          [:<>
           [:div.wrapper
            [edit]
            [:img
             {:on-click #(let [input (js/document.getElementById "profile-image")]
                           (.click input))
              :src (:profile/image-url profile-data)}]]]
          [add-circle {:on-click #(let [input (js/document.getElementById "profile-image")]
                                    (.click input))}])]
       [form/as-table {} f]
       ;; change password
       [:a.change-password
        {:on-click #(rfe/push-state :views.profile/change-password)}
        "Change password"]
       ;; save button
       [:> Button
        {:type "primary"
         :disabled (not (valid? @form-data))
         :on-click event}
        "Save"]

       [:div.bottom
        [:span.button.logout
         {:on-click #(rf/dispatch [:auth/logout])}
         "Logout"]]]]]))

(defn index []
  (r/with-let [profile (rf/subscribe [:profile/profile])]
    [show-form @profile]))
