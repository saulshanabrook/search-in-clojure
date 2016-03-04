(ns search.algorithms.base.core-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :as conjure]

            [search.core :as search]
            [search.algorithms.base.core :as base]))
(use-fixtures :once schema.test/validate-schemas)

; defined globally so that instrumenting works on it
(defn step_ [gen] (update-in gen [:index] inc))
(deftest generations-graph-test
  (conjure/instrumenting [step_]
    (let [initial-ind (-> search/Individual g/generate (assoc :traits {:value 0}))
          initial (-> search/Generation g/generate (assoc :index 0
                                                          :individuals [initial-ind]))
          value-path [:individuals 0 :traits :value]
          generations (base/generations {:initial initial
                                         :evaluate #(update-in % value-path inc)
                                         :done? #(= 9 (:index %))
                                         :step step_})]
      (conjure/verify-call-times-for step_ 0)

      (let [generation (first generations)]
        (is (= 0 (:index generation)))
        (is (= 1 (get-in generation value-path)))
        (conjure/verify-call-times-for step_ 0))

      (let [generation (second generations)]
        (is (= 1 (:index generation)))
        (is (= 2 (get-in generation value-path)))
        (conjure/verify-call-times-for step_ 1))

      (let [generation (last generations)]
        (is (= 9 (:index generation)))
        (is (= 10 (get-in generation value-path)))
        (conjure/verify-call-times-for step_ 9)))))