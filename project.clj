(defproject district0x/district-ui-web3 "1.3.2"
  :description "district UI module for handling web3 instance"
  :url "https://github.com/district0x/district-ui-web3"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[io.github.district0x/cljs-web3-next "0.2.0-SNAPSHOT"]
                 [mount "0.1.16"]
                 [org.clojure/clojurescript "1.11.4"]
                 [re-frame "0.10.5"]]

  :clean-targets ^{:protect false} ["target" "tests-output"]

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :npm {:dependencies [[web3 "1.7.3"]]
        :devDependencies [[karma "4.1.0"]
                          [karma-chrome-launcher "2.2.0"]
                          [karma-cli "2.0.0"]
                          [karma-cljs-test "0.1.0"]]}

  :profiles {:dev {:dependencies [
                                  ; [org.clojure/clojure "1.10.1"]
                                  [org.clojure/clojure "1.11.1"]
                                  [cider/piggieback "0.5.3"]
                                  [nrepl "0.9.0"]
                                  [day8.re-frame/test "0.1.5"]]
                   :plugins [[lein-cljsbuild "1.1.8"]
                             [lein-doo "0.1.11"]
                             [lein-npm "0.6.2"]
                             [lein-ancient "0.6.15"]]}}

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-output/tests.js"
                                   :output-dir "tests-output"
                                   :main "tests.runner"
                                   :optimizations :none}}]})
