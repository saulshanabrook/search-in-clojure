(ns search.graphs.problems.odd-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.problems.test-utils :refer [finishes]]))
(use-fixtures :once schema.test/validate-schemas)

(deftest ^:slow odd-test
  (finishes ['search.graphs.problems.odd/graph 'search.graphs.algorithms.genetic/graph]))
