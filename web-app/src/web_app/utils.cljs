(ns web-app.utils)

(defn generate_nickname []
  (let [nouns ["Hope" "Dream" "Party" "Jam"]
        persons ["Mathias" "Achim" "Magnus" "Thor_Atle" "Christian" "Jan_William" "Sindre" "Ronny" "Emil" "Kenneth" "Thanks" "Alf-Gunnar" "Daniel"]
        person (rand-nth persons)
        person-last-letter (clojure.string/join (take-last 1 person))
        noun (rand-nth nouns)]
    (str person (when (not (= person-last-letter "s")) "s") "." noun)
    ))

(defn teleporter-factory []
  {:uuid (cljs.core/random-uuid)
   :nickname (generate_nickname)})

(defn get-random-teleporters [num-teleporters]
  (into [] (repeatedly num-teleporters teleporter-factory)))

(defn is-touch?
  "Returns true if this device supports touch events"
  []
  ;; this is considered bad practice. If we can find
  ;; another way to do it, we should consider it
  (js* "'ontouchstart' in window"))
