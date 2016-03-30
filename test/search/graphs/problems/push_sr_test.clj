(ns search.graphs.problems.push-sr-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.problems.test-utils :refer [finishes]]))
(use-fixtures :once schema.test/validate-schemas)

(deftest ^:slow double-test
  (finishes ['search.graphs.problems.push-sr/double-graph 'search.graphs.algorithms.genetic/graph]))

(deftest ^:slow plus-six-test
  (finishes ['search.graphs.problems.push-sr/plus-six-graph 'search.graphs.algorithms.genetic/graph]))
