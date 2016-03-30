(ns search.graphs.base.done-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.schemas :as schemas]
            [search.graphs.base.done :as done]))
(use-fixtures :once schema.test/validate-schemas)

(deftest max-generations-test
  (let [gen-with-index #(-> schemas/Generation g/generate (assoc :index %))
        ->max-generations-inner #(done/max-generations {:max_ %})]
    (is ((->max-generations-inner 0) (gen-with-index 0)))
    (is ((->max-generations-inner 0) (gen-with-index 1)))
    (is ((->max-generations-inner 1) (gen-with-index 0)))
    (is (not ((->max-generations-inner 2) (gen-with-index 0))))
    (is ((->max-generations-inner 2) (gen-with-index 1)))))

(deftest any-trait-test
  (let [trait :value
        ind-with-trait-value #(-> schemas/Individual g/generate (assoc :traits {trait %}))
        generation #(-> schemas/Generation g/generate (assoc :individuals (into #{} (map ind-with-trait-value %))))
        done? (done/any-trait {:traits->done? #(-> % trait (= 10))})]
    (is (done? (generation [10])))
    (is (not (done? (generation [0]))))
    (is (done? (generation [0 10])))))
