(ns search.graphs.push-sr-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.push-sr :as push-sr]))
(use-fixtures :once schema.test/validate-schemas)

(deftest traits->done?-test
  (let [t->d? (push-sr/traits->done? {:wiggle-room 0.1})
        vals->d? #(t->d? (zipmap (range) %))]
    (is (vals->d? [0 0.001 -0.01 0.1]))
    (is (not (vals->d? [0 0.2])))))

(deftest difference-squared-test
  (are [in exp] (= exp (apply push-sr/difference-squared in))
    [0 0] 0
    [1 0] 1
    [0 1] 1
    [-1 1] 4))
