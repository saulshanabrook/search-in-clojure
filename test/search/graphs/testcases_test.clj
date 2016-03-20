(ns search.graphs.testcases-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.graphs.testcases :as testcases]))
(use-fixtures :once schema.test/validate-schemas)


(deftest testcases->traits-test
  (let [->traits (testcases/testcases->traits {:test-cases {0 0, 1 1, 2 4}
                                               :test-fn (fn [gen in] (+ gen in))
                                               :test-output->trait-value #(Math/abs (- %1 %2))})]
   (are [genome traits] (= traits (->traits genome))
    0 {0 0
       1 0
       2 2}
    1 {0 1
       1 1
       2 1}
    2 {0 2
       1 2
       2 0})))

(deftest difference-squared-test
  (are [in exp] (= exp (apply testcases/difference-squared in))
    [0 0] 0
    [1 0] 1
    [0 1] 1
    [-1 1] 4))
