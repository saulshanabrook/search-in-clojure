(ns search.config.core-test
  (:require [clojure.test :refer :all]
            [schema.test :as st]

            [search.config.core :as config]))

(st/deftest ->config-test
  (is (config/->config) "config should be created with the right schema"))

(st/deftest config->algorithm-test
  (let [_config (config/->config {:algorithm :hi})]
    (is (= :hi (config/config->algorithm _config)))))
