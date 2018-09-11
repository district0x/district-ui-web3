(ns district.ui.web3.effects
  (:require
   [re-frame.core :refer [reg-fx dispatch]]
   [district.ui.web3.ethereum-provider :as eth-provider]
   [district.ui.web3.utils :as utils :refer [web3-legacy?]]))

;;
;; ::authorize-ethereum-provider
;;
;; Attempts to authorize an ethereum provider as proposed in EIP-1102
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
;; :on-legacy - If there is no ethereum provider, and a legacy provider, dispatches the provided event
;;
;; Notes:
;;
;; - :on-accept doesn't include the full ethereum provider. This can
;;   be found at (aget js/window "ethereum")
;;
;; - ::authorize-ethereum-provider does not handle the legacy
;;   implementation, which can be handled in the :on-legacy dispatch.

(reg-fx
  ::authorize-ethereum-provider
  (fn [{:keys [:on-accept :on-reject :on-error :on-legacy]}]
   (cond
    (eth-provider/supports-ethereum-provider?)
    (doto (-> js/window .-ethereum .enable) ;; js/Promise
      (.then
       #(dispatch (conj on-accept %1))
       #(dispatch (conj on-reject %1))))
    (web3-legacy?)
    (dispatch on-legacy)
    :else
    (dispatch on-error))))
