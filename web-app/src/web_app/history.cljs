(ns web-app.history)

(defn back []
  (.back js/window.history))
