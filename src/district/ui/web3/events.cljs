(ns district.ui.web3.events
  (:require
    [cljs-web3.core :as web3]
    [district.ui.web3.queries :as queries]
    [re-frame.core :refer [reg-event-fx trim-v]]))

(def interceptors [trim-v])

(reg-event-fx
  ::start
  interceptors
  (fn [_ [{:keys [:wait-for-inject-ms] :as opts}]]
    (if (aget js/window "web3")
      {:dispatch [::create-web3 opts]}
      ;; Sometimes web3 gets injected with delay, so we'll give it one more chance
      {:dispatch-later [{:ms (or wait-for-inject-ms 1500) :dispatch [::create-web3 opts]}]})))


(reg-event-fx
  ::create-web3
  interceptors
  (fn [{:keys [:db]} [{:keys [:url]}]]
    (let [web3-injected? (boolean (aget js/window "web3"))
          web3 (if web3-injected?
                 (new (aget js/window "Web3") (web3/current-provider (aget js/window "web3")))
                 (web3/create-web3 url))
          result {:web3 web3 :web3-injected? web3-injected?}]
      {:db (queries/assoc-web3 db result)
       :dispatch [::web3-created result]})))


(reg-event-fx
  ::web3-created
  (constantly nil))


(reg-event-fx
  ::stop
  interceptors
  (fn [{:keys [:db]}]
    (when-let [web3 (queries/web3 db)]
      (web3/reset web3))
    {:db (queries/dissoc-web3 db)}))








