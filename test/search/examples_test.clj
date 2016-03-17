(ns search.examples-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.core :as search]
            [search.algorithms.hill-climb :as hill-climb]
            [search.algorithms.genetic :as genetic]
            [search.examples.list :as ex-list]
            [search.examples.push-true :as ex-push-true]))
(use-fixtures :once schema.test/validate-schemas)

(defmacro finishes
  [graphs]
  `(is
    (-> {:graph-symbols ~graphs}
      search/->search
      search/search->generations
      doall
      seq)))

(deftest list-hill-climb-test
  (finishes `(ex-list/graph hill-climb/graph)))

(deftest list-genetic-algorithm-test
  (finishes `(ex-list/graph genetic/graph)))

(deftest push-true-test
  (finishes `(ex-push-true/graph genetic/graph)))
