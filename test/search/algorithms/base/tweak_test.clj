(ns search.algorithms.base.tweak-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.algorithms.base.tweak :as tweak]))
(use-fixtures :once schema.test/validate-schemas)

(deftest tweak-genome-test
  (let [tweak-genome (fn tweak-genome-fn
                      ; breeds two parents, by creating two children, one with
                      ; the sum of their parents and one with the product
                      [first second]
                      [(+ first second)
                       (* first second)])
        tweak_ (tweak/tweak-genome {:f tweak-genome
                                    :n-parents 2})
        ->individual #(assoc (g/generate search/Individual) :genome %)
        first-parent (->individual 3)
        second-parent (->individual 4)
        parents [first-parent second-parent]
        parent-ids (map :id parents)
        [first-child second-child] (tweak_ (cycle parents))]
    (is (= 7 (:genome first-child)))
    (is (= 12 (:genome second-child)))
    (is (= parent-ids (:parents-ids first-child)))
    (is (= parent-ids (:parents-ids second-child)))))
