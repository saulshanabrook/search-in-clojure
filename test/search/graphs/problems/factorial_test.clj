(ns search.graphs.problems.factorial-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.problems.test-utils :refer [finishes]]))
(use-fixtures :once schema.test/validate-schemas)

(deftest ^:slow clojush-test
  (finishes ['search.graphs.problems.factorial/clojush-graph]))
