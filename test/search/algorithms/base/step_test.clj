(ns search.algorithms.base.step-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.algorithms.base.step :as step]))
(use-fixtures :once schema.test/validate-schemas)

(deftest breed->-test
  (let [n 10
        individual #(g/generate search/Individual)

        ; breed function creates new individuals that are all
        ; the first individual from the last generation
        step (step/breed-> {:n n :breed #(-> % :individuals first repeat)})
        generation (->
                    search/Generation
                    g/generate
                    (assoc :index 0
                           :individuals (repeatedly n individual)))
        next-gen (step generation)
        first-individual (-> generation :individuals first)]
    (is (= (assoc generation :index 1
                             :individuals (repeat n first-individual))
           next-gen))))

(deftest select-and-tweak-test
  (let [->individual #(->
                       search/Individual
                       g/generate
                       (assoc :id %))]
    (testing "tweak"
      (let [tweak (fn [_]
                    (cycle
                      [[(->individual "first") (->individual "second")]
                       [(->individual "third") (->individual "fourth")]]))
            bread (step/select-and-tweak {:select first :tweak tweak})
            generation (g/generate search/Generation)
            next-individuals (bread generation)
            next-ids (map :id next-individuals)]
        (is (= ["first" "second" "third" "fourth"] (take 4 next-ids))))
      (testing "select"
        (let [generation (->
                          search/Generation
                          g/generate
                          (assoc :individuals [(->individual "first") (->individual "second")]))

              select (fn [individuals]
                      (cycle [(first individuals) (first individuals) (second individuals)]))
              bread (step/select-and-tweak {:select select :tweak (fn [a] a)})
              _ (s/set-fn-validation! false)
              next-individuals (bread generation)
              _ (s/set-fn-validation! true)
              next-ids (map :id next-individuals)]
          (is (= '("first" "first" "second" "first") (take 4 next-ids))))))))
