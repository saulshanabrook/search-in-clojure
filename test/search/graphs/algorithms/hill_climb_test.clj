(ns search.graphs.algorithms.hill-climb-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.core :as search]
            [search.graphs.algorithms.test-utils :refer [is-algorithm]]
            [search.graphs.algorithms.hill-climb :as hill-climb]))

(use-fixtures :once schema.test/validate-schemas)

(deftest hill-climb-test
  (testing "Can compile graph"
    (is-algorithm hill-climb/graph
      {:->genome (s/=> search/Genome)
       :mutate (s/=> search/Genome search/Genome)
       :genome->traits (s/=> search/Traits search/Genome)
       :select (s/=> search/Individual [search/Individual])
       :done? (s/=> s/Bool search/Generation)})))
