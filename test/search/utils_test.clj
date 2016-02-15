(ns search.utils-test
  (:require [clojure.test :refer :all]
            [schema.test :as st]

            [search.utils :as utils]))

(def sample-value 1)

(st/deftest value->symbol-test
  (is (= 'clojure.core/get-in (utils/value->symbol get-in)))
  (is (= 'search.utils-test/sample-value (utils/value->symbol sample-value))))
