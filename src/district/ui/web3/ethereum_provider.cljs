(ns district.ui.web3.ethereum-provider
 " 
  Implements functions for authorizing the ethereum provider, as defined by proposal EIP-1102.
  
  Blog Post Outlining Functionality (outdated implementation, but explains the proposal):
  https://medium.com/metamask/https-medium-com-metamask-breaking-change-injecting-web3-7722797916a8

  Proposal EIP-1102 Link (with baseline implementation):
  https://eips.ethereum.org/EIPS/eip-1102)
 "
  (:require
   [district.ui.web3.utils :as utils :refer []]))


(defn supports-ethereum-provider?
  "Determines whether the browser has the window.ethereum object. All
  browsers are encouraged to implement this object with the method
  `.send` to invoke an authorization dialog as defined by EIP-1102."
  []
  (some-> js/window .-ethereum .-send))


(defn full-provider
  "Retrieves the full ethereum provider.
   
  Notes:

  - Full provider only exists after it has been enabled through
  `::district.ui.web3.effects/authorize-ethereum-provider`."
  []
  (aget js/window "ethereum"))
