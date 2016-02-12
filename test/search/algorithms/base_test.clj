(ns search.algorithms.base-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :as conjure]
            [com.rpl.specter :as sp]

            [search.schemas :as schemas]
            [search.algorithms.base :as base]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)

; defined globally so that instrumenting works on it
(defn step_ [gen] (update-in gen [:index] inc))
(deftest step-until-end-test
  (conjure/instrumenting [step_]
    (let [initial-ind (-> schemas/Individual g/generate (assoc :traits {:value 0}))
          initial #(-> schemas/Generation g/generate (assoc :run-id %
                                                            :index 0
                                                            :individuals [initial-ind]))
          done? #(= 9 (:index %))
          value-path [:individuals 0 :traits :value]
          evaluate #(update-in % value-path inc)
          algorithm (base/step-until-end initial evaluate done? step_)
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

(deftest generate-initial-test
  (let [->genome (fn [] :test)
        n 10
        ->initial (base/generate-initial ->genome n)
        id_ "_"
        run-id "_"]
    (conjure/stubbing [utils/id id_]
      (is (=
           {:index 0
            :run-id run-id
            :individuals (repeat n {:genome :test
                                    :id id_
                                    :parents-ids []
                                    :traits {}})}
           (->initial run-id))))))

(deftest evaluate-genome-test
  (let [genomes [[1, 2, 3],
                 [1, 2, 3, 4]]
        ind-with-genome #(-> schemas/Individual g/generate (assoc :genome %))
        generation (-> schemas/Generation g/generate (assoc :individuals (map ind-with-genome genomes)))

        genome->traits #(hash-map :value (count %))
        evaluated-generation ((base/evaluate-genome genome->traits) generation)
        values  (sp/select [:individuals sp/ALL :traits :value] evaluated-generation)]
    (is (= [3, 4] values))))

(deftest done?-max-generations-test
  (let [gen-with-index #(-> schemas/Generation g/generate (assoc :index %))]
    (is ((base/done?-max-generations 0) (gen-with-index 0)))
    (is ((base/done?-max-generations 0) (gen-with-index 1)))
    (is ((base/done?-max-generations 1) (gen-with-index 0)))
    (is (not ((base/done?-max-generations 2) (gen-with-index 0))))
    (is ((base/done?-max-generations 2) (gen-with-index 1)))))

(deftest done?-max-trait-test
  (let [trait :value
        ind-with-trait-value #(-> schemas/Individual g/generate (assoc :traits {trait %}))
        generation #(-> schemas/Generation g/generate (assoc :individuals (map ind-with-trait-value %)))
        done? (partial base/done?-max-trait :value)]
    (is ((done? 0) (generation [0])))
    (is (not ((done? 10) (generation [0]))))
    (is ((done? 10) (generation [0 10])))
    (is ((done? 10) (generation [0 20])))))

(deftest step-breed-test
  (let [n 10
        step (base/step-breed n first)
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

(deftest select-and-tweak-breed-test
  (let [individuals [{:id "1"
                      :parents-ids []
                      :genome 1
                      :traits {}}
                     {:id "2"
                      :parents-ids []
                      :genome 1
                      :traits {}}]
        tweak {:n-parents 1 :fn inc}
        bread (base/select-tweak-breed second tweak)
        child (bread individuals)]
    (is (= ["2"] (:parents-ids child)))
    (is (= 2 (:genome child)))))
