(ns search.examples-test
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
  (finishes ['search.examples.list/graph 'search.algorithms.hill-climb/graph]))

(deftest list-genetic-algorithm-test
  (finishes ['search.examples.list/graph 'search.algorithms.genetic/graph]))

(deftest push-true-test
  (finishes ['search.examples.push-true/graph 'search.algorithms.genetic/graph]))
