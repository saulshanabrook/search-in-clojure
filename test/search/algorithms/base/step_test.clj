(ns search.algorithms.base.step-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.schemas :as schemas]
            [search.algorithms.base.step :as step]))
(use-fixtures :once schema.test/validate-schemas)

(deftest breed->-test
  (let [n 10
        step (step/breed-> n first)
        individual #(g/generate schemas/Individual)
        generation (->
                    schemas/Generation
                    g/generate
                    (assoc :index 0
                           :individuals (repeatedly n individual)))
        next-gen (step generation)
        first-individual (-> generation :individuals first)]
    (is (= (assoc generation :index 1
                             :individuals (repeat n first-individual))
           next-gen))))

(deftest select-and-tweak->breed-test
  (let [individuals [{:id "1"
                      :parents-ids []
                      :genome 1
                      :traits {}}
                     {:id "2"
                      :parents-ids []
                      :genome 1
                      :traits {}}]
        tweak {:n-parents 1 :fn inc}
        bread (step/select-and-tweak->breed second tweak)
        child (bread individuals)]
    (is (= ["2"] (:parents-ids child)))
    (is (= 2 (:genome child)))))
