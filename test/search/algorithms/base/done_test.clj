(ns search.algorithms.base.done-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.algorithms.base.done :as done]))
(use-fixtures :once schema.test/validate-schemas)

(deftest max-generations-test
  (let [gen-with-index #(-> search/Generation g/generate (assoc :index %))
        ->max-generations-inner #(done/max-generations {:max_ %})]
    (is ((->max-generations-inner 0) (gen-with-index 0)))
    (is ((->max-generations-inner 0) (gen-with-index 1)))
    (is ((->max-generations-inner 1) (gen-with-index 0)))
    (is (not ((->max-generations-inner 2) (gen-with-index 0))))
    (is ((->max-generations-inner 2) (gen-with-index 1)))))

(deftest max-trait-test
  (let [trait :value
        ind-with-trait-value #(-> search/Individual g/generate (assoc :traits {trait %}))
        generation #(-> search/Generation g/generate (assoc :individuals (map ind-with-trait-value %)))
        done? #(done/max-trait {:name trait :max_ %})]
    (is ((done? 0) (generation [0])))
    (is (not ((done? 10) (generation [0]))))
    (is ((done? 10) (generation [0 10])))
    (is ((done? 10) (generation [0 20])))))
