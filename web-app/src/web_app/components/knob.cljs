(ns web-app.components.knob
  (:require [reagent.core :as r]
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

(defn- model-changed [interacting? data value-step]
  (fn [_ _ _ new-value]
    (when-not @interacting?
      (let [new-rotation (value->rotation new-value value-step)]
        (swap! data assoc :rotate/rotation new-rotation)))))

(defn show-value [model]
  [:div.value
   @model])

(defn overload [overloaded?]
  [:div.overload
   {:style (merge
            {:width "1rem"
             :height "1rem"
             :border-radius "1rem"
             :display "inline-block"
             :background-color "green"}
            (if @overloaded?
              {:box-shadow "0 0 5px #D0342C"
               :background-color "red"}))}])

(defn knob [{rotate-step :rotate/step
             rotate-sensitivity :rotate/sensitivity
             rotate-start :rotate/start
             rotate-end :rotate/end
             initial-value :value
             value-min :value/min
             value-max :value/max
             overload? :overload?
             on-change :on-change
             change :change
             model :model
             :or {rotate-step 1
                  rotate-sensitivity 20
                  rotate-start 0
                  rotate-end (+ 90 90 90)
                  value-min 0
                  value-max 100}
             :as props}]
  (r/with-let [;; declare model and overload? in here
               ;; if it is declared in the destrucuring the javascript
               ;; produced loses the reference, and the model/overload? ratom
               ;; no longer works
               model (or model (r/atom nil))
               overload? (or overload? (r/atom false))
               _ (reset! model initial-value)
               ;; we need the total distance the rotation can be
               distance (+ (js/Math.abs rotate-start)
                           (js/Math.abs rotate-end))
               ;; how much do we need to step in the rotation to hit a new value
               value-step (/ (double distance) value-max)
               data (r/atom {:rotate/rotation (value->rotation @model value-step)
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
               mouse-move (fn [e]
                            (let [{:keys [x y]} @storage
                                  [new-x new-y] [(.-pageX e) (.-pageY e)]
                                  diff-x (js/Math.abs (- new-x x))
                                  diff-y (js/Math.abs (- new-y y))
                                  {:rotate/keys [rotation start end]} @data
                                  value @model
                                  sensitivity (/ 20.0 rotate-sensitivity)]
                              (reset! storage {:x new-x
                                               :y new-y})
                              ;; we move the knob if there is movement on the x
                              ;; axis or the y axis
                              (if (> diff-x diff-y)
                                (let [step (* sensitivity (- new-x x))]
                                  (swap! data assoc :rotate/rotation (new-rotation rotation start end step))
                                  (when-let [v (rotation->value value value-step rotation)]
                                    ;; swap and check
                                    (let [updated (reset! model v)]
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
                                    (let [updated (reset! model v)]
                                      (when (and on-change
                                                 (not= value updated))
                                        ;; (println :y  :value value :update-value (:value updated))
                                        (on-change v))))))))
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
               _ (reset! functions {:mouse-up mouse-up
                                    :mouse-move mouse-move})
               watch-key (random-uuid)
               _ (add-watch model watch-key (model-changed interacting? data value-step))]
    [:div.knob
     [overload overload?]
     [show-value model]
     [:div.arc
      [arc {:radius 20
            :x 40
            :y 40
            :start-angle rotate-start
            :data data}]]
     [:div.area
      {:on-mouse-down (fn [e]
                        (reset! storage {:x (.-pageX e)
                                         :y (.-pageY e)})
                        (reset! interacting? true)
                        (js/window.addEventListener "mousemove" mouse-move)
                        (js/window.addEventListener "mouseup" mouse-up))}
      [wheel (merge
              props
              {:data data})]]]
    (finally
      ;; clean up the watcher
      (remove-watch model watch-key))))
