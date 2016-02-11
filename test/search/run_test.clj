(ns search.run-test
  (:require [clojure.test :refer :all]
            [schema.test :as st]

            [search.run :as run]
            [search.config.core :as config]))

(st/deftest config->run-test
  (let [config_ (config/->config)
        run_ (run/config->run config_)]
    (is (= (:config run_) config_))
    (is ((comp not clojure.string/blank?) (:id run_)))))
