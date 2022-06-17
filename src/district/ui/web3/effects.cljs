(ns district.ui.web3.effects
  (:require
   [re-frame.core :refer [reg-fx dispatch]]
   [district.ui.web3.ethereum-provider :as eth-provider]
   [district.ui.web3.utils :as utils :refer [web3-legacy?]]))

(defn authorize []
  (try
    (let [eth-send (aget js/window "ethereum" "send")]
     (if eth-send
       (eth-send "eth_requestAccounts")
       (js/Promise.reject "No ethereum send fn")))
    (catch js/Error e
      (js/Promise.reject (str "Error when calling eth_requestAccounts" e)))))

;;
;; ::authorize-ethereum-provider
;;
;; Attempts to authorize an ethereum provider as proposed in EIP-1102
;; When done first time, this requires user interaction (therefore not suitable for CI tests)
;; If succeeds (user gives permission e.g. in MetaMask), will emit event with the now authorized provider `window.ethereum`
;;
;; Keyword Parameters:
;;
;; :on-accept - If the ethereum provider is accepted, dispatches the
;; provided event.
;;
;; :on-reject - If the ethereum provider is rejected, dispatches the
;; provided event.
;;
;; :on-error - If there is no ethereum provider, and no legacy provider, dispatches the provided event
;;
;; Notes:
;;
;; - :on-accept doesn't include the full ethereum provider. This can
;;   be found at (aget js/window "ethereum")
;;
;; - EIP-1102 previously called ethereum.enable(), this has since been
;;   deprecated. The new method is to call
;;   ethereum.send("eth_requestAccounts")

(reg-fx
  ::authorize-ethereum-provider
  (fn [{:keys [:on-accept :on-reject :on-error]}]
    (if (eth-provider/supports-ethereum-provider?)
      (.then (authorize) #(dispatch (conj on-accept (eth-provider/full-provider))) #(dispatch (conj on-reject %1)))
      (dispatch on-error))))
