(ns search.graphs.problems.push-true-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.problems.test-utils :refer [finishes]]))
(use-fixtures :once schema.test/validate-schemas)

(deftest genetic-test
  (finishes ['search.graphs.problems.push-true/graph 'search.graphs.algorithms.genetic/graph]))
