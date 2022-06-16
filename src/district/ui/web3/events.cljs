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

; opts - can have 2 keys: :wait-for-inject-ms and :url (Ethereum node URL, e.g.
;        where Truffle is running)
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
     {:on-accept [::create-web3-with-user-permitted-provider opts]
      :on-reject [::create-web3-from-url opts]
      :on-error [::create-web3-from-url opts]}}))


(reg-event-fx
  ::create-web3-with-user-permitted-provider
  interceptors
  (fn [{:keys [:db]} [opts user-permitted-provider]]
    (println "::create-web3-with-user-permitted-provider" opts user-permitted-provider)
   (let [url (:url opts)
         web3 (web3/create-web3 user-permitted-provider url)
         result {:web3 web3 :url url}]
     {:db (queries/assoc-web3 db result)
      :dispatch [::web3-created result]})))


(reg-event-fx
 ::create-web3-from-url
 interceptors
 (fn [{:keys [:db]} [{:keys [:url]}]]
   (let [web3-instance (web3/create-web3 nil url)
         result {:web3 web3-instance :url url}]
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
