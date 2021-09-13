(ns app.i18n.dicts
  (:require [tongue.core :as tongue]))

(def dictionary
  {:no {:locale "Språk"
        :tongue/format-number (tongue/number-formatter {:group " "
                                                        :decimal ","})

        :number/seconds "{1}s"}
   :en {:locale "Locale"

        :number/seconds "{1}s"
        }
   :tongue/fallback :en})
