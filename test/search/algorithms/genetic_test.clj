(ns search.algorithms.genetic-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.core :as search]
            [search.algorithms.test-utils :refer [is-algorithm]]
            [search.algorithms.genetic :as genetic]))

(use-fixtures :once schema.test/validate-schemas)

(deftest genetic-test
  (testing "Can compile graph"
    (is-algorithm genetic/graph
      {:population-size s/Int
       :done? (s/=> s/Bool)
       :tweak (s/=> [search/Individual] [search/Individual])
       :select (s/=> search/Individual [search/Individual])
       :->genome (s/=> search/Genome)
       :genome->traits (s/=> search/Traits search/Genome)})))
