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
  (let [select_ (select/roulette {:trait-key :value
                                  :trait-specs {:value {:lowest? false}}})
        ->individual #(assoc (g/generate search/Individual) :traits {:value %})
        bad_ind (->individual 1)
        good_ind (->individual 10000000)]
    (is (= bad_ind (first (select_ #{bad_ind}))))
    (is (= good_ind (first (select_ #{good_ind}))))
    (is (= good_ind (first (select_ #{bad_ind good_ind}))))))

(deftest dominates-test
  (let [->select #(select/dominates {:trait-key :value
                                     :trait-specs {:value {:lowest? %}}})
        ->individual #(assoc (g/generate search/Individual) :traits {:value %})
        small_ind (->individual -10)
        large_ind (->individual 0)]
    (is (= large_ind (first ((->select false) #{large_ind}))))
    (is (= large_ind (first ((->select true) #{large_ind}))))

    (is (= small_ind (first ((->select false) #{small_ind}))))
    (is (= small_ind (first ((->select true) #{small_ind}))))

    (is (= large_ind (first ((->select false) #{small_ind large_ind}))))
    (is (= small_ind (first ((->select true) #{small_ind large_ind}))))))

(deftest lexicase-test
  (let [->ind #(assoc (g/generate search/Individual) :traits %)
        high-a (->ind {:a 1 :b nil})
        low-b (->ind {:a nil :b 0})]
    (with-redefs [clojure.data.generators/shuffle sort]
      (is (=
           [high-a low-b high-a]
           (->> {:trait-specs {:a {:lowest? false} :b {:lowest? true}}}
             select/lexicase
             (#(% #{high-a low-b}))
             (take 3)))))))

(deftest least-sum-squares-test
  (let [->ind #(assoc (g/generate search/Individual) :traits %)
        nil-ind (->ind {:x nil :y 1})
        four-ind (->ind {:x 2})
        six-ind (->ind {:x -2 :y -1 :z -1})
        inds #{nil-ind four-ind six-ind}]
    (is (= [four-ind six-ind nil-ind]
           (take 3 (select/least-sum-squares inds))))))
