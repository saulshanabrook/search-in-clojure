(ns search.algorithms.base.step-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.algorithms.base.step :as step]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)

(deftest breed->-test
  (let [n 10
        ->ind #(g/generate search/Individual)
        inds (utils/repeatedly-set n ->ind)
        generation (->
                    search/Generation
                    g/generate
                    (assoc :index 0
                           :individuals inds))]
    ; breed function that does nothing
    (let [step (step/breed-> {:n n :breed #(-> % :individuals)})
          next-gen (step generation)]
      (is (= {:index 1
              :individuals inds}
             (select-keys next-gen [:individuals :index]))))
    ; breed function that makes new random inds-ids
    (let [new-inds (utils/repeatedly-set n ->ind)
          step (step/breed-> {:n n :breed (fn [_] new-inds)})
          next-gen (step generation)]
      (is (= {:index 1
              :individuals new-inds}
             (select-keys next-gen [:individuals :index]))))))

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
                    (assoc :individuals (into #{} inds)))

        tweak-add-and-mult {:f (fn [a b] [(+ a b) (* a b)])
                             :multiple-children? true
                             :n-parents 2}
        tweak-inc {:f inc
                   :multiple-children? false
                   :n-parents 1}
        ; Should first use the `tweak-inc` and then `tweak-add-and-mult` and repeat
        s-and-t (step/select-and-tweak
                 {:select (comp cycle (partial sort-by :genome))
                  :->tweak (utils/seq->fn (cycle [tweak-inc tweak-add-and-mult]))})
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
