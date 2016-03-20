(ns search.problems-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.core :as search]))
(use-fixtures :once schema.test/validate-schemas)

(defn finishes
  [graphs]
  (is
    (-> {:graph-symbols graphs}
      search/->search
      search/search->generations
      doall
      seq)))

(deftest list-hill-climb-test
  (finishes ['search.problems.list/graph 'search.graphs.algorithms.hill-climb/graph]))

(deftest list-genetic-algorithm-test
  (finishes ['search.problems.list/graph 'search.graphs.algorithms.genetic/graph]))

(deftest push-true-test
  (finishes ['search.problems.push-true/graph 'search.graphs.algorithms.genetic/graph]))

(deftest push-sr-linear-test
  (finishes ['search.problems.push-sr-linear/graph 'search.graphs.algorithms.genetic/graph]))
