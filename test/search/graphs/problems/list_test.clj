(ns search.graphs.problems.list-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.problems.test-utils :refer [finishes]]))
(use-fixtures :once schema.test/validate-schemas)

(deftest hill-climb-test
  (finishes ['search.graphs.problems.list/graph 'search.graphs.algorithms.hill-climb/graph]))

(deftest genetic-algorithm-test
  (finishes ['search.graphs.problems.list/graph 'search.graphs.algorithms.genetic/graph]))
