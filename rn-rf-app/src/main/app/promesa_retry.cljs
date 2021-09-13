(ns app.promesa-retry
  (:require
   ["util" :refer [format]]
   [promesa.core :as p :refer [reject! resolve!] :refer-macros [let]]
   ))

(defn backoff-duration [retries-count interval]
  (+ (* interval retries-count) (rand-int interval)))

(defn- internal-retry
  [p f retries-left retries-count interval]
  (-> (f)
      (p/catch (fn [err]
                 (js/console.error (format "Catch failure before retrying (%s retried left): %s" retries-left (.-message err)))
                 (if (= 0 retries-left)
                   (p/reject! p err)
                   (let [d (backoff-duration retries-count interval)]
                     (js/console.log (format "Will wait %s ms before retrying (%s retries left, tried %s times)" d retries-left retries-count))
                     (-> (p/delay d "Retry msg ")
                         (p/then (fn [msg]
                                   (internal-retry p f (dec retries-left) (inc retries-count) interval))))))))
      (p/catch (fn [err]));this catch is needed to get the promise returned by the the previous catch in case of a retry, we catch the error cause and do nothing with it
      (p/then (fn [return]
                (p/resolve! p return))))
  p)

(defn retry
  ([f retries-left]
   (retry f retries-left 1000))
  ([f retries-left interval]
   (internal-retry (p/deferred) f retries-left 1 interval)))
