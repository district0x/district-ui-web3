(ns tests.all
  (:require
    [cljs-web3.core :as web3]
    [cljs.spec.alpha :as s]
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for]]
    [district.ui.web3.events :as events]
    [district.ui.web3.subs :as subs]
    [district.ui.web3.utils :as utils :refer [is-chrome?]]
    [district.ui.web3]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx]]))

(s/check-asserts true)

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
   
   (let [web3 (subscribe [::subs/web3])
         web3-injected? (subscribe [::subs/web3-injected?])
         web3-legacy? (subscribe [::subs/web3-legacy?])]
     
     (-> (mount/with-args
           {:web3 {:url "https://mainnet.infura.io/"
                   :wait-for-inject-ms 100}})
         (mount/start))

     (wait-for
      [::events/web3-created]
      (is (not (nil? @web3)))
      (is (= "https://mainnet.infura.io/" (aget (web3/current-provider @web3) "host")))
      ;; Assume Metamask Chrome Extension is running when using chrome-karma
      (is (true? (is-chrome?)))
      (is (true? @web3-injected?))
      ;; Latest version of metamask uses EIP-1102
      (is (false? @web3-legacy?))))))


(deftest invalid-params-tests
  (is (thrown? :default (-> (mount/with-args
                              {:web3 {:url nil}})
                          (mount/start)))))


(deftest invalid-params-tests2
  (is (thrown? :default (-> (mount/with-args
                              {:web3 {:url "https://mainnet.infura.io/"
                                      :wait-for-inject-ms "a"}})
                          (mount/start)))))

