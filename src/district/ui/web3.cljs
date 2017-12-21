(ns district.ui.web3
  (:require
    [cljs.spec.alpha :as s]
    [cljsjs.web3]
    [district.ui.web3.events :as events]
    [mount.core :as mount :refer [defstate]]
    [re-frame.core :refer [dispatch-sync]]))

(declare start)
(declare stop)
(defstate web3
  :start (start (:web3 (mount/args)))
  :stop (stop))

(s/def ::url string?)
(s/def ::wait-for-inject-ms number?)
(s/def ::opts (s/keys :req-un [::url]
                      :opt-un [::wait-for-inject-ms]))

(defn start [opts]
  (s/assert ::opts opts)
  (dispatch-sync [::events/start opts])
  opts)


(defn stop []
  (dispatch-sync [::events/stop]))


