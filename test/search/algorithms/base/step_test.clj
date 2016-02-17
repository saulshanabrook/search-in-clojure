(ns search.algorithms.base.step-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.schemas :as schemas]
            [search.algorithms.base.step :as step]))
(use-fixtures :once schema.test/validate-schemas)

(deftest breed->-test
  (let [n 10
        individual #(g/generate schemas/Individual)
        ; breed function creates new individuals that are all
        ; the first individual from the last generation
        breed #(-> % :individuals first repeat)
        step (step/breed-> n breed)
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
  (let [->individual #(->
                       schemas/Individual
                       g/generate
                       (assoc :id %))]
    (testing "tweak"
      (let [tweak (fn [_]
                    (cycle
                      [[(->individual "first") (->individual "second")]
                       [(->individual "third") (->individual "fourth")]]))
            select first
            bread (step/select-and-tweak->breed select tweak)
            generation (g/generate schemas/Generation)
            next-individuals (bread generation)
            next-ids (map :id next-individuals)]
        (is (= ["first" "second" "third" "fourth"] (take 4 next-ids))))
      (testing "select"
        (let [generation (->
                          schemas/Generation
                          g/generate
                          (assoc :individuals [(->individual "first") (->individual "second")]))

              select (fn [individuals]
                      (cycle [(first individuals) (first individuals) (second individuals)]))
              tweak (fn [a] a)
              bread (step/select-and-tweak->breed select tweak)
              _ (s/set-fn-validation! false)
              next-individuals (bread generation)
              _ (s/set-fn-validation! true)
              next-ids (map :id next-individuals)]
          (is (= '("first" "first" "second" "first") (take 4 next-ids))))))))
