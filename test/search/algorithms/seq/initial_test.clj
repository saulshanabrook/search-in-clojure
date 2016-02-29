(ns search.algorithms.seq.initial-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.algorithms.seq.initial :as initial]))
(use-fixtures :once schema.test/validate-schemas)

(deftest ->gene->genome-test
  (let [val (atom 0)
        ->genome (initial/->gene->genome {:n 3
                                          :->gene #(swap! val inc)})]
    (is (= [1 2 3] (->genome)))))
