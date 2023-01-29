(ns web-app.components.tba)


(defn tba [text component & args]
  [:div.tba
   [:div.inner
    [:h3 (or text "Coming soon")]]
   (when component
     (into [component] args))])
