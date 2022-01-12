(ns web-app.message.dispatch
  (:require [web-app.message.dispatch.app]
            [web-app.message.dispatch.jam]
            [web-app.message.dispatch.interface :as interface]
            [web-app.message.dispatch.platform]
            [web-app.message.dispatch.teleporter]))

(defn handler [msg]
  (interface/dispatch msg))
