(ns search.examples.list-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.examples.list :as ex-list]
            [search.core :as search]))
(use-fixtures :once schema.test/validate-schemas)

;
; (deftest hill-climb-config-test
;   (->
;     ex-list/hill-climb-config
;     search/config->run
;     search/run->generations
;     doall))
;
; (deftest genetic-config-test
;   (->
;     ex-list/genetic-config
;     search/config->run
;     search/run->generations
;     doall))
