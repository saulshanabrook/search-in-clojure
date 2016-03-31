(ns search.graphs.base.select-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema-generators.generators :as g]

            [search.schemas :as schemas]
            [search.graphs.base.select :as select]))

(use-fixtures :once schema.test/validate-schemas)

(deftest invert-list
  (is (= [4 2 1] (select/invert-list [1 2 4]))))

(deftest roulette-test
  (let [select_ (select/roulette {:trait-key :value
                                  :trait-specs {:value {:lowest? false}}})
        ->individual #(assoc (g/generate schemas/Individual) :traits {:value %})
        bad_ind (->individual 1)
        good_ind (->individual 10000000)]
    (is (= bad_ind (select_ #{bad_ind})))
    (is (= good_ind (select_ #{good_ind})))
    (is (= good_ind (select_ #{bad_ind good_ind})))))

(deftest dominates-test
  (let [->select #(select/dominates {:trait-key :value
                                     :trait-specs {:value {:lowest? %}}})
        ->individual #(assoc (g/generate schemas/Individual) :traits {:value %})
        small_ind (->individual -10)
        large_ind (->individual 0)]
    (is (= large_ind ((->select false) #{large_ind})))
    (is (= large_ind ((->select true) #{large_ind})))

    (is (= small_ind ((->select false) #{small_ind})))
    (is (= small_ind ((->select true) #{small_ind})))

    (is (= large_ind ((->select false) #{small_ind large_ind})))
    (is (= small_ind ((->select true) #{small_ind large_ind})))))

(deftest all-best-trait-test
  (let [->ind #(assoc (g/generate schemas/Individual) :traits {:value %})
        abt #(select/all-best-trait {:inds %
                                     :trait-key :value
                                     :trait-spec {:lowest? false}})
        are-bests #(is (= %1 (abt %2)))]
    (testing "one best"
      (let [high (->ind 10) low (->ind 0)]
        (are-bests #{high} #{high low})))

    (testing "multiple best"
      (let [high-a (->ind 10) high-b (->ind 10) low (->ind 0)]
        (are-bests #{high-a high-b} #{high-a high-b low})))

    (testing "nil worst"
      (let [high (->ind 10) low (->ind nil)]
        (are-bests #{high} #{high low})))

    (testing "multiple nil bests"
      (let [nil-a (->ind nil) nil-b (->ind nil)]
        (are-bests #{nil-a nil-b} #{nil-a nil-b})))))


(deftest lexicase-test
  (with-redefs [clojure.data.generators/shuffle sort]
    (let [->ind #(assoc (g/generate schemas/Individual) :traits %)
          lex (select/lexicase {:trait-specs {:a {:lowest? false}
                                              :b {:lowest? true}}})
          is-chosen #(is (= %1 (lex %2)))]

      (let [low-a (->ind {:a 0 :b nil}) high-a (->ind {:a 1 :b nil})]
        (is-chosen high-a #{low-a high-a}))

      (let [low-b (->ind {:a nil :b 0}) high-b (->ind {:a nil :b 1})]
        (is-chosen low-b #{low-b high-b}))

      (let [low-a (->ind {:a 0 :b nil})
            high-a-low-b (->ind {:a 1 :b 0})
            high-a-high-b (->ind {:a 1 :n 1})]
        (is-chosen high-a-low-b #{low-a high-a-low-b high-a-high-b}))

      (let [x (->ind {:a nil :b nil})
            y (->ind {:a nil :b nil})]
        (is (contains? #{x y} (lex #{x y})))))))

(deftest least-sum-squares-test
  (let [->ind #(assoc (g/generate schemas/Individual) :traits %)
        nil-ind (->ind {:x nil :y 1})
        four-ind (->ind {:x 2})
        six-ind (->ind {:x -2 :y -1 :z -1})
        inds #{nil-ind four-ind six-ind}]
    (is (= [four-ind six-ind nil-ind]
           (take 3 (select/least-sum-squares inds))))))
