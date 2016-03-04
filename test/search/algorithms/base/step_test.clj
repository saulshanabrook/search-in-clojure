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
                       (assoc :genome %))
        inds [(->individual 1) (->individual 2)]
        inds-ids (map :id inds)
        generation (->
                    search/Generation
                    g/generate
                    (assoc :individuals inds))

        tweak-add-and-mult {:f (fn [a b] [(+ a b) (* a b)])
                             :multiple-children? true
                             :n-parents 2}
        tweak-inc {:f inc
                   :multiple-children? false
                   :n-parents 1}
        tweaks (atom [tweak-add-and-mult tweak-inc])
        ; Should first use the `tweak-inc` and then `tweak-add-and-mult` and repeat
        s-and-t (step/select-and-tweak
                 {:select cycle
                  :->tweak (fn [] (first (swap! tweaks reverse)))})
        children (s-and-t generation)]
    (is (= [{:genome 2 :parents-ids #{(first inds-ids)}}; first should inc the first ind
            {:genome 3 :parents-ids (set inds-ids)} {:genome 2 :parents-ids (set inds-ids)} ; then add and multiply the last and the first
            {:genome 3 :parents-ids #{(second inds-ids)}}] ; then should increment the last
           (take 4 (map #(select-keys % [:genome :parents-ids]) children))))))

(deftest weighted-tweaks-test
  (let [first_tweak {:f identity :n-parents 23143 :multiple-children? true}
        second_tweak {:f identity :n-parents 234 :multiple-children? false}
        ->tweak #(step/weighted-tweaks
                  {:tweaks {:first first_tweak :second second_tweak}
                   :tweak-weights %})]
    (is (= first_tweak ((->tweak {:first 1 :second 0}))))
    (is (= second_tweak ((->tweak {:first 0 :second 1}))))))
