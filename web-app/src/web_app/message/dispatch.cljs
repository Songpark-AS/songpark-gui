(ns web-app.message.dispatch
  (:require [web-app.message.dispatch.teleporter]
            [web-app.message.dispatch.platform]
            [web-app.message.dispatch.jam]
            [web-app.message.dispatch.interface :as interface]))

(defn handler [msg]
  (interface/dispatch msg))
