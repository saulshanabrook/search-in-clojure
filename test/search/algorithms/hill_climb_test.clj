(ns search.algorithms.hill-climb-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.core :as search]
            [search.algorithms.test-utils :refer [is-algorithm]]
            [search.algorithms.hill-climb :as hill-climb]))

(use-fixtures :once schema.test/validate-schemas)

(deftest hill-climb-test
  (testing "Can compile graph"
    (is-algorithm hill-climb/graph
      {:->genome (s/=> search/Genome)
       :mutate (s/=> search/Genome search/Genome)
       :genome->value (s/=> search/TraitValue search/Genome)
       :done? (s/=> s/Bool)})))
