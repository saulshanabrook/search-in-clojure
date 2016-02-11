(ns search.algorithms.base-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :as conjure]

            [search.schemas :as schemas]
            [search.algorithms.base :as base]))

(use-fixtures :once schema.test/validate-schemas)

(def generated-generation (g/generate schemas/Generation))
(defn init-generation [run-id] (merge generated-generation  {:index 0 :run-id run-id}))
(defn end? [gen] (= 9 (:index gen)))
(defn step [gen] (update-in gen [:index] inc))

(deftest step-until-end-test
  (let [run-id "10"
        init-generation_ (init-generation run-id)]
    (is (= (assoc init-generation_ :index 1) (step init-generation_)))
    (conjure/instrumenting [step]
        (let [algorithm (base/step-until-end init-generation end? step)
              generations (algorithm run-id)]
          (conjure/verify-call-times-for step 0)
          (is (= init-generation_ (first generations)))
          (conjure/verify-call-times-for step 0)
          (is (= (assoc init-generation_ :index 1) (second generations)))
          (conjure/verify-call-times-for step 1)
          (is (= (assoc init-generation_ :index 9) (last generations)))
          (conjure/verify-call-times-for step 9)))))
