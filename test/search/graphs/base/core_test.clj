(ns search.graphs.base.core-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :as conjure]
            [com.rpl.specter :as sp]

            [search.core :as search]
            [search.graphs.base.core :as base]))
(use-fixtures :once schema.test/validate-schemas)

; defined globally so that instrumenting works on it
(defn step_ [gen] (update-in gen [:index] inc))
(deftest generations-graph-test
  (conjure/instrumenting [step_]
    (let [initial-ind (-> search/Individual g/generate (assoc :traits {:value 0}))
          initial (-> search/Generation g/generate (assoc :index 0
                                                          :individuals #{initial-ind}))
          value-path [:individuals sp/ALL :traits :value]
          generations (base/generations {:initial initial
                                         :evaluate (partial sp/transform value-path inc)
                                         :done? #(= 9 (:index %))
                                         :step step_})
          values (partial sp/select value-path)]
      (conjure/verify-call-times-for step_ 0)

      (let [generation (first generations)]
        (is (= 0 (:index generation)))
        (is (= [1] (values generation)))
        (conjure/verify-call-times-for step_ 0))

      (let [generation (second generations)]
        (is (= 1 (:index generation)))
        (is (= [2] (values generation)))
        (conjure/verify-call-times-for step_ 1))

      (let [generation (last generations)]
        (is (= 9 (:index generation)))
        (is (= [10] (values generation)))
        (conjure/verify-call-times-for step_ 9)))))
