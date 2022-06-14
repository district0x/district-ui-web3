(ns district.ui.web3.events
  (:require
    [cljs-web3-next.core :as web3]
    ["web3" :as w3]
    [district.ui.web3.queries :as queries]
    [re-frame.core :refer [reg-event-fx trim-v]]
    [district.ui.web3.effects :as effects]
    [district.ui.web3.ethereum-provider :as eth-provider]
    [district.ui.web3.utils :as utils :refer [web3-injected? web3-legacy?]]))


(def interceptors [trim-v])

(defn get-web3-instance
  ([] (get-web3-instance (eth-provider/full-provider)))
  ([provider-or-url] (new w3 provider-or-url)))

(reg-event-fx
  ::start
  interceptors
  (fn [_ [{:keys [:wait-for-inject-ms] :as opts}]]
    (if (web3-injected?)
      {:dispatch [::init-web3 opts]}
      ;; Sometimes web3 gets injected with delay, so we'll give it one more chance
      {:dispatch-later [{:ms (or wait-for-inject-ms 1500) :dispatch [::init-web3 opts]}]})))


(reg-event-fx
  ::init-web3
  interceptors
  (fn [{:keys [:db]} [{:keys [:url] :as opts}]]
    {::effects/authorize-ethereum-provider
     {:on-accept [::create-web3]
      :on-reject [::create-web3-legacy opts]
      :on-error [::create-web3-legacy opts]
      :on-legacy [::create-web3-legacy opts]}}))


(reg-event-fx
  ::create-web3
  interceptors
  (fn [{:keys [:db]}]
   (let [web3 (get-web3-instance)
         result {:web3 web3
                 :web3-injected? (web3-injected?)
                 :web3-legacy? (web3-legacy?)}]
     {:db (queries/assoc-web3 db result)
      :dispatch [::web3-created result]})))


(reg-event-fx
 ::create-web3-legacy
 interceptors
 (fn [{:keys [:db]} [{:keys [:url]}]]
   (let [web3 (if (web3-injected?)
                (new w3 (web3/current-provider (aget js/window "web3")))
                (web3/create-web3 url))
         result {:web3 web3
                 :web3-injected? (web3-injected?)
                 :web3-legacy? (web3-legacy?)}]
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
