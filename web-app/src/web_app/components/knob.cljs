(ns web-app.components.knob
  (:require ["antd" :refer [Switch]]
            [reagent.core :as r]
            [web-app.components.knob.svg :refer [arc
                                                 wheel]]))

(defn- new-rotation [rotation start end step]
  (-> (+ rotation step)
      (max start)
      (min end)))

(defn- rotation->value [value step rotation]
  (let [new-value (long (/ rotation step))]
    (if (not= new-value value)
      new-value
      nil)))

(defn- value->rotation [value step]
  (if (nil? value)
    0
    (long (* value step))))

(defn- internal-model-changed
  "The internal model has been updated. This is only run from the outside"
  [interacting? on-change data value-step]
  (fn [_ _ _ new-value]
    ;; we are not interacting directly with the knob,
    ;; it's free to update
    (when-not @interacting?
      (let [new-rotation (value->rotation new-value value-step)]
        (swap! data assoc :rotate/rotation new-rotation))
      (when on-change
        (on-change new-value)))))

(defn model-changed
  "The external model has been updated. Mirror changes to internal model"
  [internal-model]
  (fn [_ _ _ new-value]
    (reset! internal-model new-value)))

(defn show-value [value-fn model]
  [:div.value
   (value-fn @model)])

(defn overload [overloaded?]
  (when (some? overloaded?)
    [:div.overload
     [:div.led
      {:style (merge
               {:width "1rem"
                :height "1rem"
                :border-radius "1rem"
                :display "inline-block"
                :background-color "#94aa85"}
               (if @overloaded?
                 {:background-color "#d5655f"}))}]]))

(defn knob [{rotate-step :rotate/step
             rotate-sensitivity :rotate/sensitivity
             skin :skin
             rotate-start :rotate/start
             rotate-end :rotate/end
             initial-value :value
             value-min :value/min
             value-max :value/max
             overload? :overload?
             on-change :on-change
             title :title
             change :change
             model :model
             value-fn :value-fn
             :or {rotate-step 1
                  rotate-sensitivity 20
                  rotate-start 0
                  rotate-end (+ 90 90 90)
                  value-min 0
                  value-max 100
                  value-fn identity
                  skin "dark"}
             :as props}]
  (r/with-let [;; declare model and overload? in here
               ;; if it is declared in the destrucuring the javascript
               ;; produced loses the reference, and the model/overload? ratom
               ;; no longer works
               ;; this can either be a ratom or a re-frame subscription
               model (or model (r/atom nil))
               ;; this is used internally in order to make sure things are smooth
               internal-model (r/atom @model)
               _ (when (nil? @internal-model)
                   (reset! internal-model initial-value))
               ;; we need the total distance the rotation can be
               distance (+ (js/Math.abs rotate-start)
                           (js/Math.abs rotate-end))
               ;; how much do we need to step in the rotation to hit a new value
               value-step (/ (double distance) value-max)
               data (r/atom {:rotate/rotation (value->rotation @internal-model value-step)
                             :rotate/start rotate-start
                             :rotate/end rotate-end})
               ;; interacting? is used to control outside changes to the model
               ;; when a model is changed outside, we want to change the rotation
               ;; when the model is changed from rotating the wheel, we do not want
               ;; to trigger the watcher on the model, as it would potentially create
               ;; a roundtrip of triggers, as well as create a knob feeling where
               ;; each step is not smooth, but rather moved forward one step at a time
               ;; this is not the intended feeling of the knob which is meant to
               ;; be smooth
               interacting? (atom false)
               ;; we need to reference the mouse move and mouse up functions
               ;; in the functions themselves. we therefore store them in an atom
               functions (atom {})
               ;; for keeping track of x/y coordinates
               storage (atom {:x nil
                              :y nil})
               update-wheel (fn [{:keys [diff-x diff-y x y new-x new-y]}]
                              (let [{:rotate/keys [rotation start end]} @data
                                    value @internal-model
                                    sensitivity (/ 20.0 rotate-sensitivity)]
                               ;; we move the knob if there is movement on the x
                               ;; axis or the y axis
                               (if (> diff-x diff-y)
                                 (let [step (* sensitivity (- new-x x))]
                                   (swap! data assoc :rotate/rotation (new-rotation rotation start end step))
                                   (when-let [v (rotation->value value value-step rotation)]
                                     ;; swap and check
                                     (let [updated (reset! internal-model v)]
                                       ;; the check is needed in order to not reset
                                       ;; the model multiple times to the same
                                       ;; value and then report it multiple times
                                       (when (and on-change
                                                  (not= value updated))
                                         ;; (println :x  :value value :update-value (:value updated))
                                         (on-change v))))))
                               (if (> diff-y diff-x)
                                 (let [step (* sensitivity (- new-y y))]
                                   (swap! data assoc :rotate/rotation (new-rotation rotation start end step))
                                   (when-let [v (rotation->value value value-step rotation)]
                                     ;; swap and check
                                     (let [updated (reset! internal-model v)]
                                       (when (and on-change
                                                  (not= value updated))
                                         ;; (println :y  :value value :update-value (:value updated))
                                         (on-change v))))))))
               mouse-move (fn [e]
                            (let [{:keys [x y]} @storage
                                  [new-x new-y] [(.-pageX e) (.-pageY e)]
                                  diff-x (js/Math.abs (- new-x x))
                                  diff-y (js/Math.abs (- new-y y))]
                              (reset! storage {:x new-x
                                               :y new-y})
                              (update-wheel {:diff-x diff-x
                                             :diff-y diff-y
                                             :x x
                                             :y y
                                             :new-x new-x
                                             :new-y new-y})))
               mouse-up (fn [e]
                          ;; reset x,y coordinates to nil in order to provide
                          ;; a clean slate
                          (reset! storage {:x nil
                                           :y nil})
                          ;; we are no longer interacting, the model can now be changed
                          ;; from the outside
                          (reset! interacting? false)
                          (js/window.removeEventListener "mouseup"
                                                         (:mouse-up @functions))
                          (js/window.removeEventListener "mousemove"
                                                         (:mouse-move @functions)))
               touch-move (fn [e]
                            (let [{:keys [x y]} @storage
                                  touch (aget (.-changedTouches e) 0)
                                  [new-x new-y] [(.-pageX touch) (.-pageY touch)]
                                  diff-x (js/Math.abs (- new-x x))
                                  diff-y (js/Math.abs (- new-y y))]
                              (reset! storage {:x new-x
                                               :y new-y})
                              (update-wheel {:diff-x diff-x
                                             :diff-y diff-y
                                             :x x
                                             :y y
                                             :new-x new-x
                                             :new-y new-y})))
               touch-end (fn [e]
                          ;; reset x,y coordinates to nil in order to provide
                          ;; a clean slate
                          (reset! storage {:x nil
                                           :y nil})
                          ;; we are no longer interacting, the model can now be changed
                          ;; from the outside
                          (reset! interacting? false)
                          (js/window.removeEventListener "touchmove"
                                                         (:touch-move @functions))
                          (js/window.removeEventListener "touchend"
                                                         (:touch-end @functions)))
               _ (reset! functions {:mouse-up mouse-up
                                    :mouse-move mouse-move
                                    :touch-move touch-move
                                    :touch-end touch-end})
               watch-key-internal-model (random-uuid)
               _ (add-watch internal-model watch-key-internal-model
                            (internal-model-changed interacting? on-change data value-step))
               watch-key-model (random-uuid)
               _ (add-watch model watch-key-model (model-changed internal-model))]
    [:div.knob
     {:class skin}
     [overload overload?]
     [show-value value-fn internal-model]
     [:div.arc
      [arc {:radius 20
            :x 25
            :y 30
            :start-angle rotate-start
            :data data}
       {:width "100px"
        :height "100px"}]]
     [:div.area
      {:style {:touch-action "none"}
       :on-touch-start (fn [e]
                         (try
                           (if (= 1 (.-length (.-changedTouches e)))
                             (let [touch (aget (.-changedTouches e) 0)
                                   page-x (.-pageX touch)
                                   page-y (.-pageY touch)]
                               (reset! storage {:x page-x
                                                :y page-y})
                               (reset! interacting? true)
                               (js/window.addEventListener "touchmove" touch-move)
                               (js/window.addEventListener "touchend" touch-end)))
                           (catch js/Error e
                             (println e))))
       :on-mouse-down (fn [e]
                        (reset! storage {:x (.-pageX e)
                                         :y (.-pageY e)})
                        (reset! interacting? true)
                        (js/window.addEventListener "mousemove" mouse-move)
                        (js/window.addEventListener "mouseup" mouse-up))}
      [wheel (merge
              props
              {:data data})
       {:width "70px"
        :height "70px"}]]
     (when title
       [:h3 title])]
    (finally
      ;; clean up the watcher
      (remove-watch internal-model watch-key-internal-model)
      (remove-watch model watch-key-model))))


(defn knob-duo [{:keys [knob1 knob2 linked?] :as _prop}]
  (r/with-let [linked? (or linked? (r/atom false))
               model1 (or (:model knob1) (r/atom nil))]
    (let [knob1-adjusted (assoc knob1 :model model1)]
      [:div.knobs-duo
       (if @linked?
         (let [knob2-adjusted (assoc knob2
                                     :model model1
                                     :on-change nil
                                     :disabled? true)]
           [:<>
            ^{:key :knob1-alt} [knob knob1]
            ^{:key :knob2-alt} [knob knob2-adjusted]])
         [:<>
          ^{:key :knob1} [knob knob1]
          ^{:key :knob2} [knob knob2]])
       [:div.switch
        [:> Switch
         {:checkedChildren "Linked"
          :unCheckedChildren "Link"
          :on-change #(reset! linked? %)}]]])))
