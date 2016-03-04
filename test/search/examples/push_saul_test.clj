(ns search.examples.push-saul-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.examples.push-saul :as ex-push-saul]
            [search.core :as search]))
(use-fixtures :once schema.test/validate-schemas)

(deftest genetic-config-test
  (->
    ex-push-saul/genetic-config
    search/config->run
    search/run->generations
    doall))
