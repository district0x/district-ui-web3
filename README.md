# district-ui-web3

[![Build Status](https://travis-ci.org/district0x/district-ui-web3.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that takes care of setting up and providing [web3](https://github.com/ethereum/web3.js/) instance.

## Installation
Add `[district0x/district-ui-web3 "1.2.0"]` into your project.clj
Include `[district.ui.web3]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in their early stages, therefore
the API may change in the future.

- [district.ui.web3](#districtuiweb3)
- [district.ui.web3.subs](#districtuiweb3subs)
  - [::web3](#web3-sub)
  - [::web3-injected?](#web3-injected?-sub)
  - [::web3-legacy?](#web3-legacy?-sub)
- [district.ui.web3.events](#districtuiweb3events)
  - [::create-web3](#create-web3)
  - [::web3-created](#web3-created)
- [district.ui.web3.effects](#districtuiweb3effects)
  - [::authorize-ethereum-provider](#web3-ui-effects-authorize-ethereum-provider)
- [district.ui.web3.queries](#districtuiweb3queries)
  - [web3](#web3)
  - [web3-injected?](#web3-injected?)
  - [web3-legacy?](#web3-legacy?)
  - [assoc-web3](#assoc-web3)

## district.ui.web3
This namespace contains web3 [mount](https://github.com/tolitius/mount) module. Once you start mount, it'll take care of web3
initialisation and will put results into re-frame db.

You can pass following args to initiate this module:
* `:url` Url of Ethereum node to connect to
* `:wait-for-inject-ms` Sometimes web3 isn't injected quickly enough by browser extension before an app starts. If it's not, this module will try
to load it on second try after given milliseconds. Default: 1500

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

#### <a name="web3-sub">`::web3`
Returns web3 instance.

#### <a name="web3-injected?-sub">`::web3-injected?`
Returns true if web3 was injected by browser extension, such as
MetaMask.

#### <a name="web3-legacy?-sub">`::web3-legacy?`
Returns true if legacy web3 is available.

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

#### <a name="web3-legacy?-sub">`::web3-legacy?`
Returns true if the current browser uses the legacy method of
retrieving the web3 instance. This is true for browsers that are not
implementing EIP-1102.


## district.ui.web3.events
re-frame events provided by this module:

#### <a name="create-web3">`::create-web3-legacy [opts]`
Will create and save web3 instance, either by using one injected from a browser extension (e.g [MetaMask](https://metamask.io/)),
if available, or will create one from given `:url`. Normally you don't
need to use this event, as it's fired by `::start` while in legacy-mode.


#### <a name="create-web3">`::create-web3 [opts]`
This will first call `window.ethereum.enable()` and depending on the
extension's implementation, will prompt the user if they would like to
allow the ethereum provider. If accepted, it will create and save the
web3 instance (e.g [MetaMask](https://metamask.io/)).

If the browser does not support EIP-1102, or the ethereum provider is
denied, it will fallback to using `::create-web3-legacy` which will
attempt to instantiate a personal web3 instance.


#### <a name="web3-created">`::web3-created [opts]`
Event fired when web3 is created. Use this event to hook into event flow from your modules.
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
     :events #{::web3-events/web3-created}
     :dispatch-to [::do-something]}))
```

## district.ui.web3.effects
re-frame effects provided this module:

#### <a name="district-ui-web3-effects-authorize-ethereum-provider">`::authorize-ethereum-provider`
[EIP-1102](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1102.md)
provides web3 providers with the ability to enable a privacy-mode. The
`::authorize-ethereum-provider` effect is necessary to ask for the
correct permissions while users have privacy-mode enabled.

##### Keyword Parameters

:on-accept - If the ethereum provider is accepted, dispatches the
provided event.

:on-reject - If the ethereum provider is rejected, dispatches the
provided event.

:on-error - If there is no ethereum provider, and no legacy provider,
dispatches the provided event

:on-legacy - If there is no ethereum provider, and a legacy provider,
dispatches the provided event

##### Example

```clojure
(reg-event-fx
  ::init-web3
  interceptors
  (fn [{:keys [:db]} [{:keys [:url] :as opts}]]
    {::effects/authorize-ethereum-provider
     {:on-accept [::create-web3]
      :on-reject [::create-web3-legacy opts]
      :on-error [::create-web3-legacy opts]
      :on-legacy [::create-web3-legacy opts]}}))

```

##### Notes

- The ::authorize-ethereum-provider is automatically dispatched within
  the `re-mount` cycle.


## district.ui.web3.queries
DB queries provided by this module:
*You should use them in your events, instead of trying to get this module's
data directly with `get-in` into re-frame db.*

#### <a name="web3">`web3 [db]`
Returns web3 instance.

#### <a name="web3-injected?">`web3-injected? [db]`
Returns true if web3 was injected by browser extension, such as MetaMask.

#### <a name="web3-legacy?">`web3-legacy? [db]`
Returns true if legacy web3 is available.

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

#### <a name="web3-legacy?">`web3-legacy? [db]`
Returns true if the browser is using the legacy implementation of
resolving an ethereum provider. Note that this can mean that the
browser does not have an extension that resolves an ethereum provider.

#### <a name="assoc-web3">`assoc-web3 [db {:keys [:web3 :web3-injected? :web3-legacy?]}]`
Associates this module and returns new re-frame db.

## Development
```bash
lein deps

# To run tests and rerun on changes
export CHROME_BIN=`which chromium-browser`
lein doo chrome-headless tests
```
