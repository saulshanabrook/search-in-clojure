(ns search.algorithms.base.done-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]

            [search.schemas :as schemas]
            [search.algorithms.base.done :as done]))
(use-fixtures :once schema.test/validate-schemas)

(deftest max-generations->-test
  (let [gen-with-index #(-> schemas/Generation g/generate (assoc :index %))]
    (is ((done/max-generations-> 0) (gen-with-index 0)))
    (is ((done/max-generations-> 0) (gen-with-index 1)))
    (is ((done/max-generations-> 1) (gen-with-index 0)))
    (is (not ((done/max-generations-> 2) (gen-with-index 0))))
    (is ((done/max-generations-> 2) (gen-with-index 1)))))

(deftest max-trait->-test
  (let [trait :value
        ind-with-trait-value #(-> schemas/Individual g/generate (assoc :traits {trait %}))
        generation #(-> schemas/Generation g/generate (assoc :individuals (map ind-with-trait-value %)))
        done? (partial done/max-trait-> :value)]
    (is ((done? 0) (generation [0])))
    (is (not ((done? 10) (generation [0]))))
    (is ((done? 10) (generation [0 10])))
    (is ((done? 10) (generation [0 20])))))
