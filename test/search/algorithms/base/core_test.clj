(ns search.algorithms.base.core-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :as conjure]

            [search.schemas :as schemas]
            [search.algorithms.base.core :as base]))
(use-fixtures :once schema.test/validate-schemas)

; defined globally so that instrumenting works on it
(defn step_ [gen] (update-in gen [:index] inc))
(deftest ->algorithm-test
  (conjure/instrumenting [step_]
    (let [initial-ind (-> schemas/Individual g/generate (assoc :traits {:value 0}))
          initial #(-> schemas/Generation g/generate (assoc :run-id %
                                                            :index 0
                                                            :individuals [initial-ind]))
          done? #(= 9 (:index %))
          value-path [:individuals 0 :traits :value]
          evaluate #(update-in % value-path inc)
          algorithm (base/->algorithm initial evaluate done? step_)
          generations (algorithm "_")]
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
