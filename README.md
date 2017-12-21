# district-ui-web3

[![Build Status](https://travis-ci.org/district0x/district-ui-web3.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI, that takes care of setting up and providing [web3](https://github.com/ethereum/web3.js/) instance.

## Installation
Add `[district0x/district-ui-web3 "1.0.0"]` into your project.clj  
Include `[district.ui.web3]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## Usage
You can pass following args to initiate this module: 
* `:url` Url of Ethereum node to connect to
* `:wait-for-inject-ms` Sometimes web3 isn't injected quickly enough by browser extension before an app starts. If it's not, this module will try
to load it on second try after given milliseconds. Default: 1500 

## district.ui.web3
This namespace contains web3 [mount](https://github.com/tolitius/mount) module. Once you start mount, it'll take care of web3
initialisation and will put results into re-frame db. 

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3]))

  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}})
    (mount/start))
```

## district.ui.web3.subs
re-frame subscriptions provided by this module:

#### `::web3`
Returns web3 instance.

#### `::web3-injected?`
Returns true if web3 was injected by browser extension, such as MetaMask. 

```clojure
(ns my-district.home-page
  (:require [district.ui.web3.subs :as web3-subs]
            [re-frame.core :refer [subscribe]]))

(defn home-page []
  (let [web3-injected? (subscribe [::web3-subs/web3-injected?])]
    (fn []
      (if @web3-injected?
        [:div "This browser injected web3 instance"]
        [:div "This browser didn't inject web3 instance"]))))
```

## district.ui.web3.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::load-web3 [opts]`
Will create and save web3 instance, either by using one injected from a browser extension (e.g [MetaMask](https://metamask.io/)),
if available, or will create one from given `:url`. Normally you don't need to use this event, as it's fired by `::start`.

#### `::web3-loaded [opts]`
Event fired when web3 is loaded. Use this event to hook into event flow from your modules.  
One example using [re-frame-forward-events-fx](https://github.com/Day8/re-frame-forward-events-fx) may look like this: 

```clojure
(ns my-district.events
    (:require [district.ui.web3.events :as web3-events]
              [re-frame.core :refer [reg-event-fx]]
              [day8.re-frame.forward-events-fx]))
              
(reg-event-fx
  ::my-event
  (fn []
    {:register :my-forwarder
     :events #{::web3-events/web3-loaded}
     :dispatch-to [::do-something]}))
```

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.web3.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `web3 [db]`
Returns web3 instance.

#### `web3-injected? [db]`
Returns true if web3 was injected by browser extension, such as MetaMask.

```clojure
(ns my-district.events
    (:require [district.ui.web3.queries :as web3-queries]
              [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
  ::my-event
  (fn [{:keys [:db]}]
    (if (web3-queries/web3-injected? db)
      {:dispatch [::do-something]}
      {:dispatch [::do-other-thing]})))
```

#### `assoc-web3 [db {:keys [:web3 :web3-injected?]}]`
Associates this module and returns new re-frame db.

#### `dissoc-web3 [db]`
Cleans up this module from re-frame db. 


## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```