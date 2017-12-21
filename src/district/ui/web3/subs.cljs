(ns district.ui.web3.subs
  (:require
    [district.ui.web3.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::web3
  queries/web3)

(reg-sub
  ::web3-injected?
  queries/web3-injected?)