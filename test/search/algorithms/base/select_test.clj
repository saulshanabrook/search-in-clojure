(ns search.algorithms.base.select-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.algorithms.base.select :as select]))

(use-fixtures :once schema.test/validate-schemas)

(deftest invert-list
  (is (= [4 2 1] (select/invert-list [1 2 4]))))

(deftest roulette-test
  (let [select_ (select/roulette {:trait-name :value :lowest? false})
        ->individual #(assoc (g/generate search/Individual) :traits {:value %})
        bad_ind (->individual 1)
        good_ind (->individual 10000000)]
    (is (= bad_ind (first (select_ #{bad_ind}))))
    (is (= good_ind (first (select_ #{good_ind}))))
    (is (= good_ind (first (select_ #{bad_ind good_ind}))))))

(deftest dominates-test
  (let [->select #(select/dominates {:trait-name :value :lowest? %})
        ->individual #(assoc (g/generate search/Individual) :traits {:value %})
        small_ind (->individual -10)
        large_ind (->individual 0)]
    (is (= large_ind (first ((->select false) #{large_ind}))))
    (is (= large_ind (first ((->select true) #{large_ind}))))

    (is (= small_ind (first ((->select false) #{small_ind}))))
    (is (= small_ind (first ((->select true) #{small_ind}))))

    (is (= large_ind (first ((->select false) #{small_ind large_ind}))))
    (is (= small_ind (first ((->select true) #{small_ind large_ind}))))))
