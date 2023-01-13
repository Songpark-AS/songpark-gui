(ns fx
  (:require [re-frame.core :as rf]
            [web-app.utils :refer [get-api-url get-platform-url]]))


(comment
  (rf/dispatch [:http/put
                (get-api-url "/fx")
                {:fx.preset/name "Test 1"
                 :fx/fxs [{:fx/type :fx/gate
                           :fx.gate/threshold 10
                           :fx.gate/attack 100
                           :fx.gate/release 100}]}
                {:handler println
                 :error println}])
  )
