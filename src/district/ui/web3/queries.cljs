(ns district.ui.web3.queries)

(defn web3 [db]
  (-> db :district.ui.web3 :web3))

(defn web3-injected? [db]
  (-> db :district.ui.web3 :web3-injected?))

(defn assoc-web3 [db {:keys [:web3 :web3-injected?] :as params}]
  (assoc db :district.ui.web3 params))

(defn dissoc-web3 [db]
  (dissoc db :district.ui.web3))
