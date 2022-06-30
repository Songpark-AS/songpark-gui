(ns web-app.components.knob.svg
  (:require [clojure.string :as str]))

(def ^:private styles
  ".st0{fill:#1C1C1C;}
.st1{fill:#353635;}
.st2{fill:#010101;}
.st3{fill:#FFFFFF;}
.st4{fill:#404040;}
.st5{opacity:0.2;}
.st6{fill:#EAEAEA;}
.st7{fill:#4A4A4A;}
.st8{fill:#3A3232;}
.st9{fill:#DCD1C5;}
.st10{fill:#BCBCBC;}
.st11{fill:#D5655F;}
.st12{fill:#853A3B;}
.st13{fill:#423F3D;}
.st14{fill:#AE9885;}
.st15{fill:#94AA85;}
.st16{fill:#989898;}
.st17{fill:#3A3B3A;}
.st18{fill:#686868;}
.st19{fill:#2D2E2D;}
.st20{fill:#717171;}
.st21{fill:#232323;}
.st22{fill:#E1D8D2;}")

(defn wheel [{:keys [data
                     on-click] :as opts} & [width-height-opts]]
  [:svg
   (merge
    (when on-click
      {:on-click on-click})
    {:version "1.1", :view-box "0 0 204.95 209.78",
     :height (get width-height-opts :height "36pt")
     :width (get width-height-opts :width "36pt")
     :style {:transform (str "rotate(" (- (get @data :rotate/rotation 0)
                                          90 45) "deg)")}})
   [:style {:type "text/css"} styles]
   [:g
    [:g [:circle {:class "st0" :cx "102.31" :cy "102.64" :r "102.64"}]]
    [:g
     [:path {:class "st1" :d "M94.34,11.61c4.38-4.93,11.56-4.93,15.94,0l19.06,21.44c4.38,4.93,13.26,10.06,19.73,11.39l28.1,5.78 c6.46,1.33,10.05,7.54,7.97,13.81l-9.04,27.22c-2.08,6.26-2.08,16.51,0,22.78l9.04,27.22c2.08,6.26-1.51,12.48-7.97,13.81 l-28.1,5.78c-6.46,1.33-15.34,6.46-19.73,11.39l-19.06,21.44c-4.38,4.93-11.56,4.93-15.94,0l-19.06-21.44 c-4.38-4.93-13.26-10.06-19.73-11.39l-28.1-5.78c-6.46-1.33-10.05-7.54-7.97-13.81l9.04-27.22c2.08-6.26,2.08-16.51,0-22.78 l-9.04-27.22c-2.08-6.26,1.51-12.48,7.97-13.81l28.1-5.78c6.46-1.33,15.34-6.46,19.73-11.39L94.34,11.61z"}]]
    [:g
     [:path {:class "st2"
             :d "M102.31,198.87c-3.46,0-6.68-1.49-9.09-4.2l-19.06-21.44c-4.14-4.66-12.8-9.66-18.91-10.92l-28.1-5.78 c-3.55-0.73-6.46-2.78-8.19-5.77c-1.73-2.99-2.05-6.53-0.91-9.98l9.04-27.22c1.96-5.92,1.96-15.92,0-21.83L18.07,64.5 c-1.14-3.44-0.82-6.98,0.91-9.98c1.73-2.99,4.63-5.04,8.19-5.77l28.1-5.78c6.11-1.26,14.76-6.26,18.91-10.92l19.06-21.44 c2.41-2.71,5.64-4.2,9.09-4.2c3.45,0,6.68,1.49,9.09,4.2l19.06,21.44c4.14,4.66,12.8,9.66,18.91,10.92l28.1,5.78 c3.55,0.73,6.46,2.78,8.19,5.77c1.73,2.99,2.05,6.53,0.91,9.98l-9.04,27.22c-1.96,5.92-1.96,15.92,0,21.83l9.04,27.22 c1.14,3.44,0.82,6.98-0.91,9.98c-1.73,2.99-4.63,5.04-8.19,5.77l-28.1,5.78c-6.11,1.26-14.77,6.26-18.91,10.92l-19.06,21.44 C109,197.38,105.77,198.87,102.31,198.87z M95.46,12.6L76.41,34.04c-4.58,5.15-13.79,10.47-20.54,11.86l-28.1,5.78 c-2.7,0.56-4.9,2.1-6.19,4.33c-1.29,2.24-1.53,4.91-0.66,7.53l9.04,27.22c2.17,6.54,2.17,17.18,0,23.72l-9.04,27.22 c-0.87,2.62-0.64,5.29,0.66,7.53c1.29,2.24,3.49,3.78,6.19,4.34l28.1,5.78c6.75,1.39,15.97,6.71,20.54,11.86l19.06,21.44 c1.83,2.06,4.27,3.2,6.85,3.2c2.58,0,5.02-1.14,6.85-3.2l19.06-21.44c4.58-5.15,13.79-10.47,20.54-11.86l28.1-5.78 c2.7-0.56,4.9-2.1,6.19-4.34c1.29-2.24,1.53-4.91,0.66-7.53l-9.04-27.22c-2.17-6.54-2.17-17.18,0-23.72l9.04-27.22 c0.87-2.62,0.64-5.29-0.66-7.53s-3.49-3.78-6.19-4.33l-28.1-5.78c-6.75-1.39-15.97-6.71-20.54-11.86L109.16,12.6 c-1.83-2.06-4.27-3.2-6.85-3.2C99.73,9.41,97.29,10.54,95.46,12.6L95.46,12.6z"}]]
    [:g
     [:path {:class "st3"
             :d "M102.31,106.64c-2.21,0-4-1.79-4-4V14.19c0-2.21,1.79-4,4-4s4,1.79,4,4v88.45 C106.31,104.85,104.52,106.64,102.31,106.64z"}]]]])


(defn polar-to-cartesian [cx cy r angle-deg]
  (let [angle-rad (* (- angle-deg 90) (/ js/Math.PI 180.0))]
    {:x (+ cx (* r (js/Math.cos angle-rad)))
     :y (+ cy (* r (js/Math.sin angle-rad)))}))

(def ^:private base-arc-x 225)

(defn describe-arc [x y r start-angle end-angle]
  (let [start (polar-to-cartesian x y r (+ base-arc-x end-angle))
        end (polar-to-cartesian x y r (+ base-arc-x start-angle))
        large-arc? (if (<= (js/Math.abs (- end-angle start-angle)) 180)
                     0
                     1)]
    (str/join " " ["M" (:x start) (:y start)
                   "A" r r base-arc-x large-arc? 0 (:x end) (:y end)])))

(defn arc [{:keys [radius
                   x y
                   start-angle
                   data]
            :or {radius 50
                 x 25
                 y 25
                 start-angle 0}}
           & [width-height-opts]]
  (let [end-angle (:rotate/rotation @data)]
    [:svg
     {:version "1.1", :view-box (str/join " " [0 0 (* 4 radius) (* 4 radius)])
      :height (get width-height-opts :height (str (* 4 radius) "pt"))
      :width (get width-height-opts :width (str (* 4 radius) "pt"))
      }
     [:style {:type "text/css"} styles]
     [:g
      [:path { ;;:d "A 30 50 0 0 1 162.55 162.45"
              :d (describe-arc x y radius start-angle end-angle)
              ;; :d "M 20 20 A 10 10 0 0 0 30 30"
              :stroke "yellow"
              :fill "transparent"
              :stroke-width "2"}]]]))
