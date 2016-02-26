(ns search.algorithms.base.select-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [com.rpl.specter :as sp]

            [search.core :as search]
            [search.algorithms.base.select :as select]))
(use-fixtures :once schema.test/validate-schemas)

(deftest roulette-test
  (let [select_ (select/roulette {:trait-name :value})
        ->individual #(assoc (g/generate search/Individual) :traits {:value %})
        bad_ind (->individual 1)
        good_ind (->individual 10000000)]
    (is (= bad_ind (select_ [bad_ind])))
    (is (= good_ind (select_ [good_ind])))
    (is (= good_ind (select_ [bad_ind good_ind])))))
